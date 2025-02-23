package org.kinetiqx.prism.constants

import java.nio.file.Path

object LmosCliConstants {

    val projectDir: Path = Path.of(System.getProperty("user.home")).resolve(".lmos").resolve("cli")

    object AgentStarter {
        const val PACKAGE_NAME = "org.eclipse.lmos.starter"
        val AGENT_PROJECTS_DIRECTORY: Path = projectDir.resolve("agent_projects")
    }
}