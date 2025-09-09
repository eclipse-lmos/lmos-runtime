/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.Success
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.model.TenantService
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.tenant.TenantProvider
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LanguageTranslatorTest {

    private val languageTranslator = mockk<LanguageTranslator>()
    private val tenantProvider = mockk<TenantProvider>()
    private val translateLanguage = TranslateLanguage(languageTranslator, tenantProvider)

    @Test
    fun should_translate_text_into_detected_language(){
        val input = Input(
            "was ist magenta tv?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf()
                ),
                "detected_language" to "ENGLISH"
            )
        )
        coEvery { languageTranslator.translate(input.content, "ENGLISH") } returns "How are you doing?"
        runBlocking {
            assertEquals(
                Output(Status.CONTINUE, input.copy(content = "How are you doing?")),
                translateLanguage.execute(input)
            )
        }
    }


    @Test
    fun should_translate_text_into_default_language_if_no_language_is_detected(){
        val input = Input(
            "Wie geht es dir?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf()
                ),
                "detected_language" to ""
            )
        )
        val tenant = Tenant(
            "de",
            "ENGLISH",
            listOf(),
            "abc"
            )
        coEvery { languageTranslator.translate(input.content, "ENGLISH") } returns "How are you doing?"
        coEvery { tenantProvider.provideTenant() } returns Success(tenant)
        runBlocking {
            assertEquals(
                Output(Status.CONTINUE, input.copy(content = "How are you doing?")),
                translateLanguage.execute(input)
            )
        }
    }

    @Test
    fun should_thorw_exception_if_unable_to_translate(){
        val input = Input(
            "Wie geht es dir?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf()
                ),
                "detected_language" to "ENGLISH"
            )
        )

        coEvery { languageTranslator.translate(input.content, "ENGLISH") } returns null
        runBlocking {
            assertFailsWith<StepFailedException> { translateLanguage.execute(input)}
        }
    }
}