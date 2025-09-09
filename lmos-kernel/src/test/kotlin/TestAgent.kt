/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants

import org.eclipse.lmos.kernel.agent.Agent
import org.eclipse.lmos.kernel.agent.AgentProfile
import org.eclipse.lmos.kernel.steps.Input
import org.eclipse.lmos.kernel.steps.Output
import org.junit.jupiter.api.Test

class TestAgent : Agent() {
    override fun profile(): AgentProfile {
        TODO("Not yet implemented")
    }

    override suspend fun executeInternal(input: Input): Output {
        TODO("Not yet implemented")
    }

    @Test
    fun test_if_class_is_of_type_agent() {
        var method =
            TestAgent()::class.java.declaredMethods.find { method ->

                method?.name.equals("profile") && method?.returnType?.name.equals(AgentProfile::class.qualifiedName)

            }
        assert(method != null)
    }
}
