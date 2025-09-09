/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package prompt

import org.eclipse.lmos.kernel.prompt.PromptTemplate
import org.eclipse.lmos.kernel.prompt.PromptTemplateRepository

class NoOpPromptTemplateRepository : PromptTemplateRepository {
    override suspend fun findPromptTemplate(promptTemplateId: String): PromptTemplate? {
        TODO("Not yet implemented")
    }

    override suspend fun savePromptTemplateForTenant(promptTemplate: PromptTemplate) {
        TODO("Not yet implemented")
    }
}