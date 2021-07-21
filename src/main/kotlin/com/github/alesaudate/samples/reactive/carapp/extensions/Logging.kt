package com.github.alesaudate.samples.reactive.carapp.extensions

import org.slf4j.LoggerFactory

fun trace(msg: String, vararg arg: Any) = LoggerFactory.getLogger(getCallerClass()).trace(msg, arg)

fun debug(msg: String, vararg arg: Any) = LoggerFactory.getLogger(getCallerClass()).debug(msg, arg)

fun debug(msg: String, throwable: Throwable) = LoggerFactory.getLogger(getCallerClass()).debug(msg, throwable)

fun info(msg: String, vararg arg: Any) = LoggerFactory.getLogger(getCallerClass()).info(msg, arg)

fun warn(msg: String, t: Throwable) = LoggerFactory.getLogger(getCallerClass()).warn(msg, t)

fun warn(msg: String) = LoggerFactory.getLogger(getCallerClass()).warn(msg)

private fun getCallerClass() = Thread.currentThread().stackTrace.find {
    it.className.startsWith("com.github.alesaudate.samples.reactive.carapp") && !it.className.endsWith("LoggingKt")
}?.className ?: ""
