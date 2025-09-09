/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.conversations

import org.eclipse.lmos.kernel.conversations.AccessValidation.*
import org.eclipse.lmos.kernel.user.UserInformation

enum class AccessValidation {
    AUTHORIZATION_REQUIRED,
    INVALID_PROFILE_ID,
    ACCESS_ALLOWED,
}

/**
 * Validates that the user may access the current Conversation.
 */
class ConversationAccessValidator {

    fun canUserAccess(conversation: Conversation, user: UserInformation?): AccessValidation {
        if (!conversation.hasSensitiveMessages()) return ACCESS_ALLOWED
        if(user?.profileId == null && user?.partyId == null) return INVALID_PROFILE_ID
        if (user.accessToken == null) return AUTHORIZATION_REQUIRED
        if(conversation.profileId == null || conversation.partyId == null) return ACCESS_ALLOWED
        if (user.profileId != conversation.profileId || user.partyId != conversation.partyId) return INVALID_PROFILE_ID
        return ACCESS_ALLOWED
    }
}
