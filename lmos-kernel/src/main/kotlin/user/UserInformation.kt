/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.user

data class UserInformation(val profileId: String? = null, val accessToken: String? = null, val partyId: String? = null) {

    override fun toString(): String = "UserInformation(profileId=${profileId?.let { "****${it.takeLast(3)}" }}, accessToken=${accessToken?.let { "****${it.takeLast(3)}" }}, partyId=${partyId?.let { "****${it.takeLast(3)}" }})"
}
