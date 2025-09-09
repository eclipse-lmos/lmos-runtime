/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel

import org.eclipse.lmos.kernel.steps.Input

interface ModelResolver {
    suspend fun resolveModel(step: String, input: Input, forceFallback: Boolean = false): ModelResolutionResult?
    fun resolveModel(conversationId: String, tenantId: String): ModelResolutionResult
}

data class ModelResolutionResult(val modelName: String, val modelSource: String)