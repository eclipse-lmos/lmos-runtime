/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.agent.Agent


class HierarchicalChat(vararg agent: Agent) : AbstractStep() {

    private val agents = listOf(agent)
    override suspend fun executeInternal(input: Input): Output = Output(Status.CONTINUE, input)

}