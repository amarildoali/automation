package com.amarildo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {

        // Definizione delle opzioni
        Options options = new Options();

        Option scriptOption = new Option("s", "script", true, "Script da usare nel codice");
        scriptOption.setRequired(true);  // L'opzione `script` è obbligatoria
        options.addOption(scriptOption);

        Option fileOption = new Option("f", "file", true, "Percorso assoluto del file da passare come argomento");
        fileOption.setRequired(false);  // Anche `file` è obbligatorio
        options.addOption(fileOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            // Parsing degli argomenti passati
            cmd = parser.parse(options, args);

            // Estrazione dei valori
            String script = cmd.getOptionValue("script");
            Path filePath = Path.of(cmd.getOptionValue("file"));

            // Visualizzazione dei valori (o logica di utilizzo)
            System.out.println("Script selezionato: " + script);
            System.out.println("Percorso del file: " + filePath);

            Script scriptE = null;
            for (Script entry : Script.getEntries()) {
                if (script.toUpperCase().equals(entry.name())) {
                    scriptE = entry;
                }
            }

            // A questo punto puoi continuare con la tua logica utilizzando `script` e `filePath`
            switch (scriptE) {
                case EXTRACT_TXTZ -> TxtzExtractor.run(filePath);
                case null -> {
                }
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);  // Mostra l'help
            System.exit(1);
        }
    }
}
