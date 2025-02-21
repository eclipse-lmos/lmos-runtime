package org.kinetiqx.prism.credentials.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "cred",
    mixinStandardHelpOptions = true,
    description = ["Credential Management Commands"],
    subcommands = [
        AddCommand::class,
        GetCommand::class,
        ListCommand::class,
        UpdateCommand::class,
        DeleteCommand::class,
    ],
)
class CredCommand : Callable<Int> {
    override fun call(): Int {
        CommandLine.usage(this, System.out)
        return 0
    }
} 
