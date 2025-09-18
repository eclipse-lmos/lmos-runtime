/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.eclipse.lmos.runtime.core.channel.ChannelRepository
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.exception.NoChannelFoundException
import org.eclipse.lmos.runtime.core.model.custom.Channel
import org.eclipse.lmos.runtime.core.model.custom.ChannelSpecCR
import org.eclipse.lmos.runtime.core.model.custom.ChannelStatusCR
import org.eclipse.lmos.runtime.core.model.custom.ObjectMetaCR
import org.eclipse.lmos.runtime.core.model.registry.*
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Headers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [CustomResourcesController::class])
@Import(CustomResourcesControllerTest.MockConfig::class)
@ExtendWith(MockKExtension::class)
class CustomResourcesControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var channelRepository: ChannelRepository

    @Autowired
    lateinit var channelRoutingRepository: ChannelRoutingRepository

    private val tenantId = "tenant1"
    private val channelId = "channel1"
    private val subset = "test-subset"
    private val namespace = "test-namespace"

    @Test
    fun `should return list of channels`() {
        val channels =
            listOf(
                Channel(
                    apiVersion = "v1",
                    kind = "Channel",
                    metadata = ObjectMetaCR(name = channelId, namespace = "test-namespace"),
                    spec = ChannelSpecCR(),
                    status = ChannelStatusCR("resolved"),
                ),
            )
        coEvery { channelRepository.getChannels(tenantId, subset, namespace) } returns channels

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants/$tenantId/channels")
            .header(Headers.SUBSET, subset)
            .header(Headers.NAMESPACE, namespace)
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(Channel::class.java)
            .hasSize(1)
            .contains(*channels.toTypedArray())
    }

    @Test
    fun `should return single channel`() {
        val channel =
            Channel(
                apiVersion = "v1",
                kind = "Channel",
                metadata = ObjectMetaCR(name = channelId, namespace = "test-namespace"),
                spec = ChannelSpecCR(),
                status = ChannelStatusCR("resolved"),
            )
        coEvery { channelRepository.getChannel(tenantId, channelId, subset, namespace) } returns channel

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants/$tenantId/channels/$channelId")
            .header(Headers.SUBSET, subset)
            .header(Headers.NAMESPACE, namespace)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(Channel::class.java)
            .isEqualTo(channel)
    }

    @Test
    fun `should return channel routing`() {
        val routing =
            ChannelRouting(
                apiVersion = "v1",
                kind = "ChannelRouting",
                metadata =
                    Metadata(
                        name = channelId,
                        namespace = "test-namespace",
                        labels =
                            Labels(
                                channel = channelId,
                                subset = subset,
                                tenant = tenantId,
                                version = "1.0",
                            ),
                        creationTimestamp = "2024-01-01T00:00:00Z",
                        generation = 1,
                        resourceVersion = "12345",
                        uid = "uid-12345",
                    ),
                spec =
                    Spec(
                        capabilityGroups =
                            listOf(
                                CapabilityGroup(
                                    id = "cg1",
                                    name = "Group1",
                                    description = "desc",
                                    capabilities =
                                        listOf(
                                            Capability(
                                                id = "cap1",
                                                name = "Cap1",
                                                description = "desc",
                                                providedVersion = "1.0",
                                                requiredVersion = "1.0",
                                                host = "http://example.com",
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )
        coEvery { channelRoutingRepository.getChannelRouting(tenantId, channelId, subset, namespace) } returns routing

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants/$tenantId/channels/$channelId/routing")
            .header(Headers.SUBSET, subset)
            .header(Headers.NAMESPACE, namespace)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelRouting::class.java)
            .isEqualTo(routing)
    }

    @Test
    fun `should return 404 when channel not found`() {
        coEvery { channelRepository.getChannel(tenantId, channelId, subset, namespace) } throws NoChannelFoundException("bla")

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants/$tenantId/channels/$channelId")
            .header(Headers.SUBSET, subset)
            .header(Headers.NAMESPACE, namespace)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun channelRepository(): ChannelRepository = mockk()

        @Bean
        fun channelRoutingRepository(): ChannelRoutingRepository = mockk()
    }
}
