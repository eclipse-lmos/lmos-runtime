package org.eclipse.lmos.cli.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun runAtFixedRate(stopKeywords: Set<String>, pollingDurationMillis: Long, maxAttempts: Long, initialDelayMillis: Long = 0, fn: () -> String): String {
    var matched = ""
    runBlocking {
        var i = 0
        delay(initialDelayMillis)
        while (matched.isEmpty() && i++ < maxAttempts) {
            delay(pollingDurationMillis)
            val result = fn()
            matched = stopKeywords.find { result.contains(it) } ?: ""
        }
    }
    return matched
}

fun runAtFixedRate(pollingDurationMillis: Long, maxAttempts: Long, initialDelayMillis: Long = 0, fn: () -> Any, fn2: (Any) -> Boolean): Any {
    var result = Any()
    runBlocking {
        var i = 0
        delay(initialDelayMillis)
        while (i++ < maxAttempts) {
            delay(pollingDurationMillis)
            result = fn()
            if(fn2(result)) {
                break
            }
        }
    }
    return result
}

fun runAtFixedRate(pollingDurationMillis: Long, initialDelayMillis: Long = 0, fn: () -> Any, breakFn: (Any) -> Boolean, loopFn: () -> Boolean): Any {
    var result = Any()
    runBlocking {
        delay(initialDelayMillis)
        while (loopFn()) {
            delay(pollingDurationMillis)
            result = fn()
            if(breakFn(result)) {
                break
            }
        }
    }
    return result
}