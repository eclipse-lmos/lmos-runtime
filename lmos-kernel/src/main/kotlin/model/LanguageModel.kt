/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.model

import java.time.Duration

data class LanguageModel(
     val id: String,
     val provider: String,
     val url: String,
     val apiKey: String? = null,
     val modelName: String,
     var temperature: Double = 0.0,
     var topP: Double? = null,
     var maxTokens: Int? = null,
     var presencePenalty: Double? = null,
     var frequencyPenalty: Double? = null,
     var timeout: Duration? = null,
     var maxRetries: Int? = null,
     var logRequests: Boolean? = null,
     var logResponses: Boolean? = null,
     val seed: Long? = null,
     val format: String? = null
) {
     companion object {
          const val FORMAT_JSON = "json"
     }
}