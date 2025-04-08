package org.eclipse.lmos.cli

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import org.eclipse.lmos.cli.commands.agent.Agent
import org.eclipse.lmos.cli.commands.config.Config
import picocli.CommandLine


@TopCommand
@QuarkusMain
@CommandLine.Command(
    name = "lmos",
    version = ["1.0.0"],
    mixinStandardHelpOptions = true,
    subcommands = [
        Agent::class,
        Config::class
    ], description = ["LMOS Command Line Interface"]
)
class LmosCli : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("Quarkus run")
        return 0
    }

    private fun executionStrategy(parseResult: CommandLine.ParseResult): Int {
        Initializer().initialize()
        return CommandLine.RunLast().execute(parseResult)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = LmosCli()
            CommandLine(app)
                .setExecutionStrategy {
                        parseResult: CommandLine.ParseResult -> app.executionStrategy(parseResult) }
                .execute(*args)
        }
    }

}