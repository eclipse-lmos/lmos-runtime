/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.retriever

import org.eclipse.lmos.kernel.retriever.SimilarDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContextRetrieverTest {

    @Test
    fun should_be_able_to_serialize_and_deserialize_documents() {
            val originalDoc = SimilarDocument("Page1",
                metadata = hashMapOf("key1" to "value1", "key2" to "value2"),
                score = "173",
                search_metric = "IP"
            )
            val json = Json.encodeToString(originalDoc)
            val deserializedDoc = Json.decodeFromString<SimilarDocument>(json)
            assertEquals(originalDoc, deserializedDoc)
        }
}