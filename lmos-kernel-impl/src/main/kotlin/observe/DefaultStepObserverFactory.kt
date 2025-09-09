/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.observe

import org.eclipse.lmos.kernel.observe.ObservationContext
import org.eclipse.lmos.kernel.observe.StepObserver
import org.eclipse.lmos.kernel.observe.StepObserverFactory
import org.eclipse.lmos.kernel.tenant.TenantProvider
import io.micrometer.observation.ObservationRegistry

class DefaultStepObserverFactory(private val observationRegistry: ObservationRegistry, private val tenantProvider: TenantProvider, private val observationName: String) : StepObserverFactory {
    override fun create(observationContext: ObservationContext): StepObserver {
        if(observationContext.name.isEmpty()) observationContext.name = observationName
        return DefaultStepObserver(observationContext, observationRegistry, tenantProvider)
    }
}
