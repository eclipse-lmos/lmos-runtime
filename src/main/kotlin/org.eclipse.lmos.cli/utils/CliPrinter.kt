package org.eclipse.lmos.cli.utils

import picocli.CommandLine

object CliPrinter {

    fun printlnHeader(s: String) {
        CommandLine.Help.Ansi.AUTO.string(
            "@|blue $s |@"
        ).also(::println)
    }

    fun promptUserInput(s: String) {
        CommandLine.Help.Ansi.AUTO.string(
            "@|yellow $s |@"
        ).also(::print)
    }

    fun printSuccess(s: String) {
        CommandLine.Help.Ansi.AUTO.string(
            "@|bold,green $s |@"
        ).also(::println)
    }

    fun printConvOutput(role: String, message: String) {
        CommandLine.Help.Ansi.AUTO.string(
            "@|bold,cyan $role |@: $message"
        ).also(::println)
    }

    fun printError(s: String) {
        CommandLine.Help.Ansi.AUTO.string(
            "@|bold,red $s |@"
        ).also(::println)
    }
}