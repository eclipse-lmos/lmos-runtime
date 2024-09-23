/*
 * // SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.runtime.core.constants

object ApiConstants {
    object Endpoints {
        const val BASE_PATH = "/lmos/runtime/apis/v1"
        const val CHAT_URL = "/{tenantId}/chat/{conversationId}/message"
    }

    object Headers {
        const val TURN_ID = "x-turn-id"
    }
}
