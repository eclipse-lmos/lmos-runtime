/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.getOrThrow
import org.eclipse.lmos.kernel.steps.DetectLanguage.Companion.DETECTED_LANGUAGE
import org.eclipse.lmos.kernel.tenant.TenantProvider

fun interface LanguageTranslator {
    suspend fun translate(text: String, language: String): String?
}

class TranslateLanguage(
    private val languageTranslator: LanguageTranslator,
    private val tenantProvider: TenantProvider,
) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {
        val lang = input.context<String>(DETECTED_LANGUAGE).takeIf { it.isNotEmpty() } ?: geDefaultLanguage()

        return try {
            val translated = languageTranslator.translate(input.content, lang) ?: error("Failed to translate output!")
            Output(Status.CONTINUE, input.copy(content = translated))
        } catch (e: IllegalStateException) {
            throw StepFailedException("Failed to translate output!", e)
        }

    }

    private suspend fun geDefaultLanguage(): String {
        val tenant = tenantProvider.provideTenant().getOrThrow()
        return tenant.defaultLanguage
    }
}

