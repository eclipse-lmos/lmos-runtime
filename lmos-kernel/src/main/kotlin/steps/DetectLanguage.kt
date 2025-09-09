/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import java.util.*

fun interface LanguageDetector {
    fun detect(input: String): Locale?
}

class DetectLanguage(private val languageDetector: LanguageDetector) : AbstractStep() {

    companion object {
        const val DETECTED_LANGUAGE = "detected_language"
    }

    override suspend fun executeInternal(input: Input): Output {
        input.stepContext[DETECTED_LANGUAGE] = languageDetector.detect(input.content)?.language
        return Output(Status.CONTINUE, input)
    }
}

