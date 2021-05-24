package com.github.alesaudate.samples.reactive.carapp.extensions

import org.slf4j.LoggerFactory

fun Any.debug(msg: String, vararg arg: Any) = LoggerFactory.getLogger(javaClass).debug(msg, arg)

fun Any.info(msg: String, vararg arg: Any) = LoggerFactory.getLogger(javaClass).info(msg, arg)

fun Any.warn(msg: String, t: Throwable) = LoggerFactory.getLogger(javaClass).warn(msg, t)

fun Any.warn(msg: String) = LoggerFactory.getLogger(javaClass).warn(msg)
