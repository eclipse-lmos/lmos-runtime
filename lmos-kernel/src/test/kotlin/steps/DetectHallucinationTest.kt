/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.DefaultAsserter.fail

class DetectHallucinationTest {
    private val hallucinationDetector = mockk<HallucinationDetector>()
    private val detectHallucination = DetectHallucination(hallucinationDetector)

    @Test
    fun detect_hallucination(){

    }
}