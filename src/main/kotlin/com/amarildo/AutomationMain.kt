package com.amarildo

import org.apache.commons.cli.*
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val options = Options()

    // options definition
    val scriptOption =
        Option("s", "script", true, "Script to use in code").apply {
            isRequired = true // `script` is mandatori
        }
    val fileOption =
        Option("f", "file", true, "Absolute path of the file to pass as argument").apply {
            isRequired = false
        }
    options.addOption(scriptOption)
    options.addOption(fileOption)

    val parser: CommandLineParser = DefaultParser()
    val formatter = HelpFormatter()
    val cmd: CommandLine

    try {
        // arguments parsing
        cmd = parser.parse(options, args)

        // extracting values
        val script = cmd.getOptionValue("script")
        val filePath: Path? = cmd.getOptionValue("file")?.let { Path.of(it) }

        println("Selected script: $script")

        // Verifica se il `script` è valido e corrisponde a un `Script` esistente
        val scriptEnum = Script.entries.find { it.name.equals(script, ignoreCase = true) }

        // Logica di esecuzione in base al tipo di `script` selezionato
        when (scriptEnum) {
            Script.EXTRACT_TXTZ -> TxtzExtractor.run(filePath)
            null -> {
                println("Script non valido o non supportato")
            }
        }
    } catch (e: ParseException) {
        println(e.message)
        formatter.printHelp("utility-name", options) // Mostra l'help
        exitProcess(1)
    }
}
