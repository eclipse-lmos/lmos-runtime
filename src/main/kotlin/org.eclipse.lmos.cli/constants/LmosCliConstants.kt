package org.eclipse.lmos.cli.constants

import java.io.File
import java.nio.file.Path

object LmosCliConstants {

    val projectDir: Path
        get() = Path.of(System.getProperty("user.home")).resolve(".lmos").resolve("cli")

    object AgentStarterConstants {
        const val PACKAGE_NAME = "org.eclipse.lmos.starter"
        val AGENT_DIRECTORY: Path
            get() = projectDir.resolve("agents")
        val AGENT_PROJECTS_DIRECTORY: Path
            get() = AGENT_DIRECTORY.resolve("projects")
        val AGENTS_REGISTRY: Path
            get() = AGENT_DIRECTORY.resolve("registry")
    }

    object CredentialManagerConstants {
        val CREDENTIAL_DIRECTORY: Path
            get() = projectDir.resolve(".cred")
        val CREDENTIAL_CONFIG: File
            get() = CREDENTIAL_DIRECTORY.resolve("credentials.yaml").toFile()
    }

    const val PREFIX = "LLM_CONFIG"
}