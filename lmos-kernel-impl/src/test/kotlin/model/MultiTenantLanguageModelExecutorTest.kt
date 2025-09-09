/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package model

import org.eclipse.lmos.kernel.Failure
import org.eclipse.lmos.kernel.Success
import org.eclipse.lmos.kernel.impl.model.MultiTenantLanguageModelExecutor
import org.eclipse.lmos.kernel.model.*
import org.eclipse.lmos.kernel.tenant.MissingTenantException
import org.eclipse.lmos.kernel.tenant.TenantProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MultiTenantLanguageModelExecutorTest {
    private val tenantProvider= mockk<TenantProvider>()
    private val multiTenantLanguageModelExecutor = MultiTenantLanguageModelExecutor(tenantProvider, null)
    @Test
    fun should_return_tenant_language_model(){
        val tenant = Tenant(
            "de",
            "ENGLISH",
            listOf(),
            "abc"
        )
        coEvery { tenantProvider.provideTenant() } returns Success(tenant)
        runBlocking { assertEquals("abc", multiTenantLanguageModelExecutor.getLanguageModelName()) }
    }

    @Test
    fun should_fail_if_unable_to_provide_tenant() {
        coEvery { tenantProvider.provideTenant() } returns Failure(MissingTenantException())
        runBlocking {
            assertFailsWith<MissingTenantException> { multiTenantLanguageModelExecutor.getLanguageModelName() }
        }
    }

    @Test
    fun should_throw_LanguageModelException_as_language_model_is_not_registered(){
        val conversationMessage = mockk<ConversationMessage>()
        val tenant = Tenant(
            "de",
            "ENGLISH",
            listOf(),
            "abc"
        )
        coEvery { tenantProvider.provideTenant() } returns Success(tenant)
        runBlocking {
            assertThrows<LanguageModelException> { multiTenantLanguageModelExecutor.ask(listOf(conversationMessage)) }
        }

    }

    @Test
    fun should_return_success_output_if_we_pass_list_of_conversation_messages(){
        val conversationMessage = UserMessage("was ist magenta tv? ")
        val languageModelClient = mockk<LanguageModelClient>()
        val languageModel = mockk<LanguageModel>()
        val tenant = Tenant(
            "de",
            "ENGLISH",
            listOf(),
            "abc"
        )
        every { languageModelClient.getLanguageModel() } returns languageModel
        every { languageModel.id } returns "abc"
        coEvery { tenantProvider.provideTenant() } returns Success(tenant)
        val result =Success(AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?"))
        coEvery { languageModelClient.ask(listOf(conversationMessage)) } returns result
        runBlocking {
            multiTenantLanguageModelExecutor.registerClient(languageModelClient)
            assertEquals(result, multiTenantLanguageModelExecutor.ask(listOf(conversationMessage)))
        }
    }

    @Test
    fun should_return_success_output_if_we_pass_conversation_message(){
        val conversationMessage = UserMessage("was ist magenta tv? ")
        val languageModelClient = mockk<LanguageModelClient>()
        val languageModel = mockk<LanguageModel>()
        val tenant = Tenant(
            "de",
            "ENGLISH",
            listOf(),
            "abc"
        )
        every { languageModelClient.getLanguageModel() } returns languageModel
        every { languageModel.id } returns "abc"
        coEvery { tenantProvider.provideTenant() } returns Success(tenant)
        val result =Success(AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?"))
        coEvery { languageModelClient.ask(listOf(conversationMessage)) } returns result
        runBlocking {
            multiTenantLanguageModelExecutor.registerClient(languageModelClient)
            assertEquals(result, multiTenantLanguageModelExecutor.ask(conversationMessage))
        }
    }


}