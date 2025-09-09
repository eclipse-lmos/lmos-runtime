/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.observe

import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC

interface AgentTracer {
    suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String> = emptyMap(),
        fn: suspend (Tags) -> T,
    ): T
}

interface Tags {
    fun tag(key: String, value: String)

    fun tag(key: String, value: Long)

    fun tag(key: String, value: Boolean)

    fun error(ex: Throwable)
}

object NoopTags : Tags {
    override fun tag(key: String, value: String) {
        // no-op
    }

    override fun tag(key: String, value: Long) {
        // no-op
    }

    override fun tag(key: String, value: Boolean) {
        // no-op
    }

    override fun error(ex: Throwable) {
        // no-op
    }
}

/**
 * Default implementation of [AgentTracer] that sets log context.
 */
class DefaultAgentTracer : AgentTracer {

    override suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String>,
        fn: suspend (Tags) -> T,
    ): T = withLogContext(attributes) {
        fn(NoopTags)
    }
}



suspend fun <T> withLogContext(
    map: Map<String, String>,
    block: suspend kotlinx.coroutines.CoroutineScope.() -> T,
): T = withContext(MDCContext(logContext() + map), block)


fun logContext() = MDC.getCopyOfContextMap() ?: emptyMap()