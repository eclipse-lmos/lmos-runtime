/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.retriever

import kotlinx.serialization.Serializable

interface ContextRetriever {
    suspend fun findRelevant(text: String, tenantId: String, nbrDocuments: Int): List<SimilarDocument>
    suspend fun findRelevantMultiple(text: List<String>, tenantId: String, nbrDocuments: Int): List<SimilarDocument>
}

@Serializable
data class SimilarDocument(
    val page_content: String, //issue with serialization if named in camel case
    val metadata: HashMap<String, String>,
    val score: String = "",
    val search_metric: String = "",
    val collection_id: String = ""
)
