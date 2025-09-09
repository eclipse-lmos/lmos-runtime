/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.conversations

import org.eclipse.lmos.kernel.conversations.ConversationClassification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Classifications of conversations.
 */
@SerialName("BillingClassification")
@Serializable
object BillingClassification : ConversationClassification

@SerialName("FAQClassification")
@Serializable
object FAQClassification : ConversationClassification