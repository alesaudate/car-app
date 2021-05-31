package com.github.alesaudate.samples.reactive.carapp.observability

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toFlux

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
            if (result is Publisher<*>) {
                result.toFlux().shareNext().subscribe(
                    {
                        metricsRegistry.registerSuccessServiceCall(metricsName, methodName)
                    },
                    {
                        metricsRegistry.registerFailureServiceCall(metricsName, methodName)
                    }
                )
            } else {
                metricsRegistry.registerSuccessServiceCall(metricsName, methodName)
            }
            return result
        } catch (ex: Exception) {
            metricsRegistry.registerFailureServiceCall(metricsName, methodName)
            throw ex
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