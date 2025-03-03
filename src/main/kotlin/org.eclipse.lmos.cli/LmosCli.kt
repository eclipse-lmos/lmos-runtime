package org.eclipse.lmos.cli

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.inject.Inject
import org.eclipse.lmos.cli.commands.agent.Agent
import org.eclipse.lmos.cli.commands.config.Config
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENTS_REGISTRY
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_CONFIG
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_DIRECTORY
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists


@TopCommand
//@QuarkusMain
@CommandLine.Command(
    name = "lmos",
    mixinStandardHelpOptions = true,
    subcommands = [
        Agent::class,
        Config::class
    ], description = ["LMOS Command Line Interface"]
)
class LmosCli : Runnable {

    @Inject
    lateinit var factory: CommandLine.IFactory


    override fun run() {

//        run {
            Initializer().initialize()
            println("Running init for Lmos CLI Completed")
//        }
        val str = """ 
            
************************************************************
${Ansi.AUTO.string("The @|bold,blue,underline LMOS Agent Universe|@ is Calling.")}
************************************************************
            
            """.trimIndent()
        val argGet2 = arrayOf("config", "llm", "list")
        CommandLine(this, factory).execute(*argGet2)
        println(str)
        CommandLine.usage(this, System.out);
    }




//
//    @Throws(Exception::class)
//    override fun run(vararg args: String): Int {
//
//        init()
////        CommandLine.usage(this, System.out)
//
////        val agent2 = arrayOf("agent", "chat")
////
////        return CommandLine(this, factory).execute(*agent2)
////
//        return credManagerTest()
//
////        return agentTest()
//
//    }

//    private fun agentTest(): Int {
//        val agent = arrayOf("agent", "create")
//
//        val agent2 = arrayOf("agent", "create", "-t", "ARC")
//
//        CommandLine(this, factory).execute(*agent)
//        CommandLine(this, factory).execute(*agent2)
//
//
//        val arg = arrayOf("config", "llm", "list")
//        return CommandLine(this, factory).execute(*arg)
//    }
//
//    private fun credManagerTest(): Int {
////        val argCreate1 = arrayOf("config", "llm", "add", "1", "-u", "admin", "-p", "pass")
////
////        val argGet1 = arrayOf("config", "llm", "get", "1")
////        val argGet2 = arrayOf("config", "llm", "get", "2")
////
////        val argCreate2 = arrayOf("config", "llm", "add", "2", "-u", "admin", "-p", "pass")
////
////        val arg = arrayOf("config", "llm", "list")
////
////        val argDel = arrayOf("config", "llm", "delete", "all")
//
//        println("Starting Credential Manager Test")
//
//        val argCreate1 = arrayOf("config", "llm", "add", "-i", "1")
//        val argCreate2 = arrayOf("config", "llm", "add")
//
//        val argGet1 = arrayOf("config", "llm", "get", "-i", "1")
//        val argGet2 = arrayOf("config", "llm", "get")
//
//        val argList = arrayOf("config", "llm", "list")
//
//        val argDelAll = arrayOf("config", "llm", "delete")
//        val argDelAll2 = arrayOf("config", "llm", "delete", "-i", "4")
//        val argDelAll3 = arrayOf("config", "llm", "delete", "-i", "all")
//
//
////        CommandLine(this, factory).execute(*argList)
////
////        CommandLine(this, factory).execute(*argCreate1)
////        CommandLine(this, factory).execute(*argGet1)
////        CommandLine(this, factory).execute(*argGet2)
////        CommandLine(this, factory).execute(*argCreate2)
////        CommandLine(this, factory).execute(*argGet2)
////        CommandLine(this, factory).execute(*argList)
//        CommandLine(this, factory).execute(*argDelAll2)
////        CommandLine(this, factory).execute(*argDelAll2)
////        CommandLine(this, factory).execute(*argDelAll3)
//        return CommandLine(this, factory).execute(*argList)
//    }

}
