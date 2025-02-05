package org.kinetiqx.prism

import io.quarkus.picocli.runtime.annotations.TopCommand
import org.kinetiqx.prism.commands.ChatCommand
import org.kinetiqx.prism.commands.InstallCommand
import org.kinetiqx.prism.commands.ListCommand
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi



@TopCommand
@CommandLine.Command(
    name = "lmos",
    mixinStandardHelpOptions = true,
    subcommands = [
        ListCommand::class,
        InstallCommand::class,
        ChatCommand::class
    ], description = ["LMOS Command Line Interface"]
)
class LmosCli : Runnable {

    override fun run() {
        val str = """ 
            
************************************************************
${Ansi.AUTO.string("The @|bold,blue,underline LMOS Agent Universe|@ is Calling.")}
************************************************************
            
            """.trimIndent()
        println(str)
        CommandLine.usage(this, System.out);
    }
}
