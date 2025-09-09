/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package steps

import org.eclipse.lmos.kernel.impl.steps.SpringTaskFactory
import org.eclipse.lmos.kernel.steps.ClassifyIntent
import org.eclipse.lmos.kernel.steps.Step
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import kotlin.test.assertEquals

class SpringTaskFactoryTest {
    private val beanFactory = mockk<ConfigurableBeanFactory>()
    private val springTaskFactory = SpringTaskFactory(beanFactory)
    @Test
    fun should_return_bean_of_step(){
        val step = mockk<Step>()
        val stepClass: Class<out Step> = ClassifyIntent::class.java
        coEvery { beanFactory.getBean(stepClass) } returns step
        runBlocking { assertEquals(step, springTaskFactory.getStep(stepClass)) }
    }
}