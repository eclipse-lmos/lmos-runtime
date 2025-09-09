/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.observe

interface StepObserverFactory {
    fun create(observationContext: ObservationContext): StepObserver
}

data class ObservationContext(val origin: String, var name: String = "")

class NoOpStepObserverFactory : StepObserverFactory {
    override fun create(observationContext: ObservationContext): StepObserver = NoOpStepObserver()
}
