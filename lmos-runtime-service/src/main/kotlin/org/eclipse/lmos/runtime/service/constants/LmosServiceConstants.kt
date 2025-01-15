/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.constants

object LmosServiceConstants {
    object Endpoints {
        const val BASE_PATH = "/lmos/runtime/apis/v1"
        const val CHAT_URL = "/{tenantId}/chat/{conversationId}/message"
    }

    object Headers {
        const val TURN_ID = "x-turn-id"
        const val SUBSET = "x-subset"
    }
}
