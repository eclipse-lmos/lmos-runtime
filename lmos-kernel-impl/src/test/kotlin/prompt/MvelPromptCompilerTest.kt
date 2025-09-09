/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package prompt

import org.eclipse.lmos.kernel.Failure
import org.eclipse.lmos.kernel.Success
import org.eclipse.lmos.kernel.impl.prompt.MvelPromptCompiler
import org.eclipse.lmos.kernel.prompt.CompilationFailedException
import org.eclipse.lmos.kernel.prompt.PromptTemplate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MvelPromptCompilerTest {

    private val mvelPromptCompiler =MvelPromptCompiler()
    @Test
    fun should_compile_prompt_successfully(){
        val variable = mapOf("question" to "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?")
        val promptTemplate= PromptTemplate("intent_classification", "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "@{question}\n" +
                "```")
        val compiledContent = "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "Wie kann ichh über Apple TV auf diesen Dienst zugreifen? Welche Schritte muss ich ausführen?\n" +
                "```"
        assertEquals(Success(compiledContent), mvelPromptCompiler.compile(promptTemplate, variable))
    }

    @Test
    fun should_fail_to_compile_prompt(){
        val variable = mapOf("" to "")
        val promptTemplate= PromptTemplate("intent_classification", "You are a conversation classifier.\n" +
                "Given the question in the backticks, determine the intent of the question.\n" +
                "\n" +
                "If related to billing or payment, answer with \"billing\".\n" +
                "Otherwise answer with \"faq\".\n" +
                "\n" +
                "Only answer with a single word.\n" +
                "\n" +
                "```\n" +
                "@{question}\n" +
                "```")
        assertTrue(mvelPromptCompiler.compile(promptTemplate, variable) is Failure<CompilationFailedException>)
    }
}