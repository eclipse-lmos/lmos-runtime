/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Created On: 10/05/24
 * Author Name: Neeraj Mishra (neeraj.mishra@telekom-digitail.com)
 **/
package org.eclipse.lmos.kernel.services

import org.eclipse.lmos.kernel.steps.Input

interface EventLogger {
    suspend fun logConversationInitiated(input: Input)

    suspend fun logTurnStarted(input: Input)

    suspend fun <T> logTurnCompleted(response: T)
}