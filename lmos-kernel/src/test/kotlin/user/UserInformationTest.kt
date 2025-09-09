/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Created On: 23/02/24
 * Author Name: Neeraj Mishra (neeraj.mishra@telekom-digitail.com)
 **/
package com.telekom.lmos.platform.assistants.user

import org.eclipse.lmos.kernel.user.UserInformation
import org.junit.jupiter.api.Test

class UserInformationTest {

    @Test
    fun `should hash in to string`() {
        val userInformation = UserInformation("profile-id-12334", "access-token-1384947", "party-id-12456")
        assert(userInformation.toString() == "UserInformation(profileId=****334, accessToken=****947, partyId=****456)")
    }

    @Test
    fun `should handle null access token`() {
        val userInformation = UserInformation(profileId = "profile-id-12334", partyId = "party-id-12456")
        assert(userInformation.toString() == "UserInformation(profileId=****334, accessToken=null, partyId=****456)")
    }

    @Test
    fun `should handle null profile id`() {
        val userInformation = UserInformation(accessToken="access-token-1384947", partyId = "party-id-12456")
        assert(userInformation.toString() == "UserInformation(profileId=null, accessToken=****947, partyId=****456)")
    }

    @Test
    fun `should handle null party id`() {
        val userInformation = UserInformation(accessToken="access-token-1384947", profileId = "party-id-12345")
        assert(userInformation.toString() == "UserInformation(profileId=****345, accessToken=****947, partyId=null)")
    }
}