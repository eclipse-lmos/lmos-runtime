/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson3.*
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature

fun ContentNegotiationConfig.installDefaultJackson() {
    jackson {
        disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
