package org.eclipse.lmos.cli.credential

import io.quarkus.runtime.annotations.RegisterForReflection
import kotlinx.serialization.Serializable

@RegisterForReflection
@Serializable
data class Credential(
    val id: String,
    val content: String,
)

@Serializable
data class CredentialManagerConfig (
    val type: CredentialManagerType
)

enum class CredentialManagerType {
    MAC, LINUX, WIN
}
