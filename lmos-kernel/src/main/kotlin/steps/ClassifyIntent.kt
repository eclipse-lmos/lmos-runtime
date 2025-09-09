/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps


fun interface IntentClassifier {
    suspend fun classify(question: String): String?
}

class ClassifyIntent(private val intentClassifier: IntentClassifier) : AbstractStep() {

    companion object {
        const val INTENT_CLASSIFICATION = "intent_classification"
    }

    override suspend fun executeInternal(input: Input): Output {
        val classification = intentClassifier.classify(input.content)
        input.stepContext[INTENT_CLASSIFICATION] = classification
        return Output(Status.CONTINUE, input)
    }
}