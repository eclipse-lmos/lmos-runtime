/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.model.custom

data class ObjectMetaCR(
    val name: String? = null,
    val namespace: String? = null,
    val labels: Map<String, String>? = null,
    val annotations: Map<String, String>? = null,
)

data class RequiredCapabilityCR(
    val id: String? = null,
    val name: String,
    val version: String,
    val strategy: String? = null,
)

data class ChannelSpecCR(
    val requiredCapabilities: List<RequiredCapabilityCR> = emptyList(),
)

data class ChannelStatusCR(
    val resolveStatus: String? = null,
    val unresolvedRequiredCapabilities: List<RequiredCapabilityCR>? = null,
)

data class Channel(
    val apiVersion: String,
    val kind: String,
    val metadata: ObjectMetaCR,
    val spec: ChannelSpecCR,
    val status: ChannelStatusCR? = null,
)
