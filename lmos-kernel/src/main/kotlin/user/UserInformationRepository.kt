/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.user

interface UserInformationRepository {
    fun getUserInformation(tenantId: String, conversationId: String): UserInformation

    fun storeUserInformation(tenantId: String, conversationId: String, userInformation: UserInformation)

}