/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.observe

import org.eclipse.lmos.kernel.agent.AgentProfile
import org.eclipse.lmos.kernel.getOrNull
import org.eclipse.lmos.kernel.observe.ObservationContext
import org.eclipse.lmos.kernel.observe.StepObserver
import org.eclipse.lmos.kernel.observe.ObservationInput
import org.eclipse.lmos.kernel.steps.Output
import org.eclipse.lmos.kernel.tenant.TenantProvider
import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.withContext

class DefaultStepObserver(
    private val observationContext: ObservationContext,
    private val observationRegistry: ObservationRegistry,
    private val tenantProvider: TenantProvider
) : StepObserver {

    companion object {
        const val TENANT_KEY = "tenant"
        const val TYPE_KEY = "type"
        const val TYPE_VALUE_AGENT = "AGENT"
        const val TYPE_VALUE_STEP = "STEP"
        const val ORIGIN_KEY = "origin"
        const val IDENTIFIER_KEY = "identifier"
        const val UNKNOWN = "UNKNOWN"
    }

    override suspend fun observeSteps(observationInput: ObservationInput, action: suspend () -> Output): Output = Observation.createNotStarted(observationContext.name, observationRegistry)
            .contextualName(observationContext.origin)
            .lowCardinalityKeyValue(TENANT_KEY, resolveTenant())
            .lowCardinalityKeyValue(TYPE_KEY, TYPE_VALUE_AGENT)
            .lowCardinalityKeyValue(ORIGIN_KEY, observationContext.origin)
            .lowCardinalityKeyValue(IDENTIFIER_KEY, observationContext.origin)
            .observeAndWait(observationRegistry){ action() }

    override suspend fun observe(observationInput: ObservationInput, action: suspend () -> Output): Output = Observation.createNotStarted(observationContext.name, observationRegistry)
            .contextualName(resolveName(observationInput))
            .lowCardinalityKeyValue(TENANT_KEY, resolveTenant())
            .lowCardinalityKeyValue(TYPE_KEY, resolveStepType(observationInput))
            .lowCardinalityKeyValue(ORIGIN_KEY, observationContext.origin)
            .lowCardinalityKeyValue(IDENTIFIER_KEY, resolveName(observationInput))
            .observeAndWait(observationRegistry) { action() }

    /**
     * Ref: https://github.com/micrometer-metrics/micrometer/issues/4754#issuecomment-1952927968
     */
    private suspend fun <T> Observation.observeAndWait(
        observationRegistry: ObservationRegistry,
        block: suspend () -> T
    ): T {
        start()
        return withContext(
            openScope().use { observationRegistry.asContextElement() }
        ) {
            try {
                block()
            } catch (error: Throwable) {
                error(error)
                throw error
            } finally {
                stop()
            }
        }
    }

    private fun resolveName(observationInput: ObservationInput): String = if (observationInput.agentName.isNotEmpty()) observationInput.agentName else observationInput.stepClass.simpleName

    private fun resolveStepType(observationInput: ObservationInput): String {
        if (observationInput.agentName.isNotEmpty()) return TYPE_VALUE_AGENT

        var agentProfileMethod = observationInput.stepClass.declaredMethods.find { method ->
            method?.name.equals("profile") && method?.returnType?.name.equals(AgentProfile::class.qualifiedName)
        }
        return agentProfileMethod?.let { TYPE_VALUE_AGENT } ?: run { TYPE_VALUE_STEP }
    }

    private suspend fun resolveTenant(): String = tenantProvider.provideTenant().getOrNull()?.tenantId ?: UNKNOWN
}
