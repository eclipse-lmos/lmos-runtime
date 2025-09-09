/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.ConfigurationException
import org.eclipse.lmos.kernel.agent.Agent
import org.eclipse.lmos.kernel.steps.ClassifyIntent.Companion.INTENT_CLASSIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC

class ExecuteAgent(private val agents: List<Agent>) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {
        val classification = input.context<String>(INTENT_CLASSIFICATION, "")
        val agent = agents.find { it.canHandle(classification) }
            ?: throw ConfigurationException("Missing Assistant for $classification!")

        return logAgentStepName(agent) { agent.execute(input) }
    }

    private suspend fun <T> logAgentStepName(agent: Agent, block: suspend CoroutineScope.() -> T): T {
        val stepName = agent::class.simpleName.toString()
        val currentContext = (MDC.getCopyOfContextMap() ?: emptyMap()) + ("step" to stepName)
        return withContext(MDCContext(currentContext), block)
    }
}