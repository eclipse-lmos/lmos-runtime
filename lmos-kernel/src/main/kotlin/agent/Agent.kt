/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.agent

import org.eclipse.lmos.kernel.steps.AbstractStep
import kotlinx.serialization.Serializable

abstract class Agent : AbstractStep() {

    open fun canHandle(intent: String): Boolean = false
    abstract fun profile(): AgentProfile
}

@Serializable
data class AgentProfile(
    val name: String,
    val purpose: String,
    val dp: String
)
