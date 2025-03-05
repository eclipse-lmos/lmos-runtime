package org.eclipse.lmos.cli.outbound

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * A CDI-free REST client implementation using Java's built-in HttpClient
 */
class GenericRestClient {
    private val httpClient = HttpClient.newBuilder().build()
    private val objectMapper = ObjectMapper()

    fun create(baseUrl: String): DynamicClient {
        return DynamicClientImpl(baseUrl, httpClient, objectMapper)
    }

    interface DynamicClient {
        fun get(): Response
        fun post(data: String): Response
    }

    private class DynamicClientImpl(
        private val baseUrl: String,
        private val httpClient: HttpClient,
        private val objectMapper: ObjectMapper
    ) : DynamicClient {
        
        override fun get(): Response {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .header("Accept", MediaType.APPLICATION_JSON)
                .build()
                
            val httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            return Response.status(httpResponse.statusCode())
                .entity(httpResponse.body())
                .build()
        }
        
        override fun post(data: String): Response {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Accept", MediaType.APPLICATION_JSON)
                .build()
                
            val httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            return Response.status(httpResponse.statusCode())
                .entity(httpResponse.body())
                .build()
        }
    }
}