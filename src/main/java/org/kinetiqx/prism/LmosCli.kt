package org.kinetiqx.prism

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.inject.Inject
import org.kinetiqx.prism.agent.commands.AgentCommand
import org.kinetiqx.prism.credentials.commands.CredCommand
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi


//@TopCommand
@QuarkusMain
@CommandLine.Command(
    name = "lmos",
    mixinStandardHelpOptions = true,
    subcommands = [
        AgentCommand::class,
        CredCommand::class
    ], description = ["LMOS Command Line Interface"]
)
class LmosCli : Runnable, QuarkusApplication {

    @Inject
    lateinit var factory: CommandLine.IFactory


    override fun run() {
        val str = """ 
            
************************************************************
${Ansi.AUTO.string("The @|bold,blue,underline LMOS Agent Universe|@ is Calling.")}
************************************************************
            
            """.trimIndent()
        println(str)
        CommandLine.usage(this, System.out);
    }

    @Throws(Exception::class)
    override fun run(vararg args: String): Int {

//        credManagerTest()

        return agentTest()

    }

    private fun agentTest(): Int {
        val agent = arrayOf("agent", "create")

        val agent2 = arrayOf("agent", "create", "-t", "ARC")

        CommandLine(this, factory).execute(*agent)
        CommandLine(this, factory).execute(*agent2)


        val arg = arrayOf("cred", "list")
        return CommandLine(this, factory).execute(*arg)
    }

    private fun credManagerTest(): Int {
        val argCreate1 = arrayOf("cred", "add", "1", "-u", "admin", "-p", "pass")

        val argGet1 = arrayOf("cred", "get", "1")
        val argGet2 = arrayOf("cred", "get", "2")

        val argCreate2 = arrayOf("cred", "add", "2", "-u", "admin", "-p", "pass")

        val arg = arrayOf("cred", "list")

        val argDel = arrayOf("cred", "delete", "all")

        CommandLine(this, factory).execute(*arg)

        CommandLine(this, factory).execute(*argCreate1)
        CommandLine(this, factory).execute(*argGet1)
        CommandLine(this, factory).execute(*argGet2)
        CommandLine(this, factory).execute(*argCreate2)
        CommandLine(this, factory).execute(*argGet2)
        CommandLine(this, factory).execute(*arg)
        CommandLine(this, factory).execute(*argDel)
        return CommandLine(this, factory).execute(*arg)
    }

}
