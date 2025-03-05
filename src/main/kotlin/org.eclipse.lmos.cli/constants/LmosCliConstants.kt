package org.eclipse.lmos.cli.constants

import java.io.File
import java.nio.file.Path

object LmosCliConstants {

    val PROJECT_ROOT_DIR: Path by lazy {
        Path.of(System.getProperty("user.home")).resolve(".lmos").resolve("cli")
    }

    val LOG_DIR: Path by lazy {
        PROJECT_ROOT_DIR.resolve("logs")
    }

    val LOG_FILE_PATH: Path by lazy {
        LOG_DIR.resolve("lmos-cli.log")
    }

    object AgentStarterConstants {
        const val PACKAGE_NAME = "org.eclipse.lmos.starter"
        private val AGENT_DIRECTORY: Path by lazy {
            val resolve = PROJECT_ROOT_DIR.resolve("agents")
            resolve
        }
        val AGENT_PROJECTS_DIRECTORY: Path by lazy {
            val resolved = AGENT_DIRECTORY.resolve("projects")
            resolved
        }
        val AGENTS_REGISTRY: Path by lazy {
            val resolved = AGENT_DIRECTORY.resolve("registry")
            resolved
        }
    }

    object CredentialManagerConstants {
        val CREDENTIAL_DIRECTORY: Path by lazy {
            val resolved = PROJECT_ROOT_DIR.resolve(".cred")
            resolved
        }
        val CREDENTIAL_CONFIG: File by lazy {
            val resolved = CREDENTIAL_DIRECTORY.resolve("credentials.yaml").toFile()
            resolved
        }

        val MODEL_IDS: File by lazy {
            val resolved = CREDENTIAL_DIRECTORY.resolve("models.yaml").toFile()
            resolved
        }
    }

    const val PREFIX = "LLM_CONFIG_"
}