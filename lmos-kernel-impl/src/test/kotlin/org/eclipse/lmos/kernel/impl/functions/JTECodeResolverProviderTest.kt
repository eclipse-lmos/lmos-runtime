/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path
import kotlin.test.assertNotNull

class JTECodeResolverProviderTest {

    private val templatesFolder = mockk<File>()
    private val jteCodeResolverProvider = JTECodeResolverProvider(templatesFolder)

    @Test
    fun should_return_valid_object(){
        every { templatesFolder.toPath() } returns Path("mock/dir")
        assertNotNull(jteCodeResolverProvider.provide())
        verify(exactly = 1) { templatesFolder.toPath() }
    }
}