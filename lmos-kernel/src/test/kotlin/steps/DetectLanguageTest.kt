/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class DetectLanguageTest {
    private val languageDetector = mockk<LanguageDetector>()
    private val detectLanguage = DetectLanguage(languageDetector)
    @Test
    fun should_detect_language_input(){
        val input = Input("Wie geht es dir?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "notco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                )
            )
        )
        coEvery { languageDetector.detect(input.content) } returns Locale.GERMAN
        runBlocking {
            detectLanguage.execute(input)
        }
        assertEquals(Locale.GERMAN.language, input.stepContext["detected_language"])
    }
}