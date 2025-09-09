/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.language

import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import org.eclipse.lmos.kernel.steps.LanguageDetector
import org.slf4j.LoggerFactory
import java.util.*

class LinguaLanguageDetector(
    private val detector: com.github.pemistahl.lingua.api.LanguageDetector =
        LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.GERMAN).build()
) : LanguageDetector {

    private val log = LoggerFactory.getLogger(javaClass)


    override fun detect(input: String): Locale? = try {
            val detection = detector.detectLanguageOf(input)
            Locale.forLanguageTag(detection.isoCode639_1.name.lowercase()).also {
                log.debug("Detected language = $it")
            }
        } catch (e: Exception) {
            log.error("Failed to detect language!", e)
            null
        }
}
