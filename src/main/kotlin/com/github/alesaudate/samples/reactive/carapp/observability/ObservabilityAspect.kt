package com.github.alesaudate.samples.reactive.carapp.observability

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Observed(
    val metricsName: String
)

@Aspect
@Component
class ObservabilityAspect(
    val metricsRegistry: MetricsDataProvider
) {

    @Around("@annotation(com.github.alesaudate.samples.reactive.carapp.observability.Observed)")
    fun interceptObserved(joinPoint: ProceedingJoinPoint): Any? {

        val metricsName = getMetricsName(joinPoint)
        val methodName = getMethodName(joinPoint)

        metricsRegistry.registerServiceCall(metricsName, methodName)

        try {
            val result = joinPoint.proceed()
            when (result) {
                is Mono<*> -> return registerListenersOnMono(result, metricsName, methodName)

                // this project doesn't have any methods annotated by @{link Observed} that
                // return a Flux
                // is Flux<*> -> return registerListenersOnFlux(result, metricsName, methodName)
                else -> metricsRegistry.registerSuccessServiceCall(metricsName, methodName)
            }
            return result
        } catch (ex: Exception) {
            metricsRegistry.registerFailureServiceCall(metricsName, methodName)
            throw ex
        }
    }

    private fun registerListenersOnMono(mono: Mono<*>, metricsName: String, methodName: String): Mono<out Any> {

        return mono.doOnNext {
            metricsRegistry.registerSuccessServiceCall(metricsName, methodName)
        }.doOnError {
            metricsRegistry.registerFailureServiceCall(metricsName, methodName)
        }
    }

    private fun getMethodName(joinPoint: ProceedingJoinPoint) = getMethodSignature(joinPoint).name

    private fun getMetricsName(joinPoint: ProceedingJoinPoint) = getAnnotation(joinPoint).metricsName

    private fun getAnnotation(joinPoint: ProceedingJoinPoint): Observed {
        val signature = getMethodSignature(joinPoint)
        return signature.method.getAnnotation(Observed::class.java)
    }

    private fun getMethodSignature(joinPoint: ProceedingJoinPoint) = joinPoint.signature as MethodSignature
}
