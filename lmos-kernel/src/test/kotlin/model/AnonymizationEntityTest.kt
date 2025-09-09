/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.model

import org.eclipse.lmos.kernel.model.AnonymizationEntity
import org.eclipse.lmos.kernel.retriever.SimilarDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AnonymizationEntityTest {


        @Test
        fun should_be_able_to_serialize_and_deserialize_documents() {
            val originalEntity = AnonymizationEntity("abc", "xyz", "pqr")
            val json = Json.encodeToString(originalEntity)
            val deserializedEntity = Json.decodeFromString<AnonymizationEntity>(json)
            assertEquals(originalEntity, deserializedEntity)
    }
}