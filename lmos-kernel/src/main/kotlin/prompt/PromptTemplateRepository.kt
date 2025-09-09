/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.prompt


interface PromptTemplateRepository {
    suspend fun findPromptTemplate(promptTemplateId: String): PromptTemplate?

    suspend fun savePromptTemplateForTenant(promptTemplate: PromptTemplate)
}
