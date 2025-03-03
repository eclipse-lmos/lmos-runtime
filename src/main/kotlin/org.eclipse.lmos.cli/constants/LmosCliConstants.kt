package org.eclipse.lmos.cli.constants

import java.io.File
import java.nio.file.Path

object LmosCliConstants {

    val projectDir: Path by lazy {
        Path.of(System.getProperty("user.home")).resolve(".lmos").resolve("cli")
    }

    object AgentStarterConstants {
        const val PACKAGE_NAME = "org.eclipse.lmos.starter"
        val AGENT_DIRECTORY: Path by lazy {
            val resolve = projectDir.resolve("agents")
            println("DEBUG: AGENT_DIRECTORY: $resolve")
            resolve
        }
        val AGENT_PROJECTS_DIRECTORY: Path by lazy {
            val resolved = AGENT_DIRECTORY.resolve("projects")
            println("DEBUG: AGENT_PROJECTS_DIRECTORY: $resolved")
            resolved
        }
        val AGENTS_REGISTRY: Path by lazy {
            val resolved = AGENT_DIRECTORY.resolve("registry")
            println("DEBUG: AGENTS_REGISTRY: $resolved")
            resolved
        }
    }

    object CredentialManagerConstants {
        val CREDENTIAL_DIRECTORY: Path by lazy {
            val resolved = projectDir.resolve(".cred")
            println("DEBUG: CREDENTIAL_DIRECTORY: $resolved")

            resolved
        }
        val CREDENTIAL_CONFIG: File by lazy {
            val resolved = CREDENTIAL_DIRECTORY.resolve("credentials.yaml").toFile()
            println("DEBUG: CREDENTIAL_DIRECTORY: $resolved")
            resolved
        }
    }

    const val PREFIX = "LLM_CONFIG:"
}