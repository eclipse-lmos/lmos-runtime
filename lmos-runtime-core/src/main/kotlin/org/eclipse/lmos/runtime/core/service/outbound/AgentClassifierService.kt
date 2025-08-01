/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.Conversation

interface AgentClassifierService {
    suspend fun classify(
        conversation: Conversation,
        agents: List<Agent>,
        tenant: String,
    ): ClassificationResult
}
