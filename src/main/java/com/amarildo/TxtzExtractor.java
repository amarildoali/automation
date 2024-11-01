package com.amarildo;


import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TxtzExtractor {

    public static void run(@NotNull Path filePath) {
        Path cartellaParent = filePath.getParent();
        String txtzFilePath = String.valueOf(filePath);
        String destinationFolder = cartellaParent.toString();  // Inserisci qui il percorso di destinazione

        try {
            extractTxtzFile(txtzFilePath, destinationFolder);
            processExtractedFiles(txtzFilePath, destinationFolder);
            System.out.println("Operazione completata con successo.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore durante l'operazione: " + e.getMessage());
        }
    }

    // Metodo per estrarre il contenuto di un file .txtz
    private static void extractTxtzFile(String txtzFilePath, String destinationFolder) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(txtzFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File outputFile = new File(destinationFolder, entry.getName());
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }

    // Metodo per processare i file estratti
    private static void processExtractedFiles(String txtzFilePath, String destinationFolder) throws IOException {
        File extractedFolder = new File(destinationFolder);

        // Cancella il file metadata.opf
        File metadataFile = new File(extractedFolder, "metadata.opf");
        if (metadataFile.exists() && metadataFile.isFile()) {
            metadataFile.delete();
        }

        // Rinomina il file index.txt con il nome del file .txtz originale ma con estensione .md
        File indexFile = new File(extractedFolder, "index.txt");
        String originalFileName = Paths.get(txtzFilePath).getFileName().toString();
        String baseFileName = originalFileName.replaceFirst("\\.txtz$", "");
        String sanitizedFolderName = sanitizeFolderName(baseFileName);

        if (indexFile.exists() && indexFile.isFile()) {
            File renamedFile = new File(extractedFolder, sanitizedFolderName + ".md");
            indexFile.renameTo(renamedFile);

            // Rinomina la cartella images con il nome del file originale
            File imagesFolder = new File(extractedFolder, "images");
            File renamedImagesFolder = new File(extractedFolder, sanitizedFolderName);
            if (imagesFolder.exists() && imagesFolder.isDirectory()) {
                imagesFolder.renameTo(renamedImagesFolder);
            }

            // Crea un file di backup e aggiorna i riferimenti alle immagini
            File backupFile = new File(extractedFolder, sanitizedFolderName + "_backup.md");
            updateMarkdownImageReferences(renamedFile, backupFile, sanitizedFolderName);
        }
    }

    // Metodo per aggiornare i riferimenti alle immagini nel file markdown
    private static void updateMarkdownImageReferences(File originalFile, File backupFile, String newImagesFolderName) throws IOException {
        // Pattern regex per trovare i riferimenti alle immagini nella forma "](images/000XXX.png)"
        Pattern pattern = Pattern.compile("\\]\\(images/(\\S+?\\.[^\\)]+)\\)");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(originalFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Aggiorna i riferimenti alle immagini sostituendo "images" con il nuovo nome della cartella
                String updatedLine = pattern.matcher(line).replaceAll("](" + newImagesFolderName + "/$1)");
                writer.write(updatedLine);
                writer.newLine();
            }
        }
    }

    // Metodo per normalizzare il nome della cartella
    @NotNull
    private static String sanitizeFolderName(@NotNull String folderName) {
        // Converti tutto in minuscolo, sostituisci spazi con underscore e rimuovi caratteri speciali
        return folderName.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")  // Sostituisce ogni carattere non alfanumerico con underscore
                .replaceAll("_+", "_");       // Rimuove eventuali underscore ripetuti
    }
}
