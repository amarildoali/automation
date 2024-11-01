package com.amarildo

import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class TxtzExtractor {
    companion object {
        fun run(filePath: Path?) {
            if (filePath == null) {
                println("filePath cannot be null")
                return
            }

            val txtzFilePath = filePath.toString()
            val destinationFolder = filePath.parent.toString()

            try {
                extractTxtzFile(txtzFilePath, destinationFolder)
                processExtractedFiles(txtzFilePath, destinationFolder)
                println("Operation completed successfully.")
            } catch (e: IOException) {
                e.printStackTrace()
                System.err.println("Error during operation: ${e.message}")
            }
        }

        // Method to extract the contents of a .txtz file
        private fun extractTxtzFile(
            txtzFilePath: String,
            destinationFolder: String,
        ) {
            ZipInputStream(FileInputStream(txtzFilePath)).use { zipIn ->
                var entry: ZipEntry?
                while (zipIn.nextEntry.also { entry = it } != null) {
                    val outputFile = File(destinationFolder, entry!!.name)
                    if (entry!!.isDirectory) { // if the destination folder does not exist -> create it
                        outputFile.mkdirs()
                    } else {
                        outputFile.parentFile.mkdirs()
                        BufferedOutputStream(FileOutputStream(outputFile)).use { bos ->
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                                bos.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    zipIn.closeEntry()
                }
            }
        }

        // Method to process the extracted files
        private fun processExtractedFiles(
            txtzFilePath: String,
            destinationFolder: String,
        ) {
            val extractedFolder = File(destinationFolder)

            // Delete the metadata.opf file
            val metadataFile = File(extractedFolder, "metadata.opf")
            if (metadataFile.exists() && metadataFile.isFile) {
                metadataFile.delete()
            }

            // Rename index.txt with the original .txtz file name but with .md extension
            val indexFile = File(extractedFolder, "index.txt")
            val originalFileName = Paths.get(txtzFilePath).fileName.toString()
            val baseFileName = originalFileName.replaceFirst("\\.txtz$".toRegex(), "")
            val sanitizedFolderName = sanitizeFolderName(baseFileName)

            if (indexFile.exists() && indexFile.isFile) {
                val renamedFile = File(extractedFolder, "$sanitizedFolderName.md")
                indexFile.renameTo(renamedFile)

                // Rename the images folder with the original file name
                val imagesFolder = File(extractedFolder, "images")
                val renamedImagesFolder = File(extractedFolder, sanitizedFolderName)
                if (imagesFolder.exists() && imagesFolder.isDirectory) {
                    imagesFolder.renameTo(renamedImagesFolder)
                }

                // Create a backup file and update image references
                val backupFile = File(extractedFolder, "${sanitizedFolderName}_optimized.md")
                updateMarkdownImageReferences(renamedFile, backupFile, sanitizedFolderName)
            }
        }

        // Method to update image references in the markdown file
        private fun updateMarkdownImageReferences(
            originalFile: File,
            backupFile: File,
            newImagesFolderName: String,
        ) {
            // Regex pattern to find image references in the form "](images/000XXX.png)"
            val pattern = Regex("\\]\\(images/(\\S+?\\.[^\\)]+)\\)")

            BufferedReader(FileReader(originalFile)).use { reader ->
                BufferedWriter(FileWriter(backupFile)).use { writer ->
                    reader.lineSequence().forEach { line ->
                        // Update image references replacing "images" with the new folder name
                        val updatedLine =
                            pattern
                                .replace(line, "]($newImagesFolderName/$1)")
                                .replace("\\(", "(") // remove '\' before curly
                                .replace("\\)", ")")
                                .replace("Figur", "figur") // lowercase Figura 1.XX
                        writer.write(updatedLine)
                        writer.newLine()
                    }
                }
            }
        }

        // Method to sanitize folder name
        private fun sanitizeFolderName(folderName: String): String {
            // Convert to lowercase, replace spaces with underscores, and remove special characters
            return folderName
                .lowercase()
                .replace("[^a-z0-9]".toRegex(), "_") // Replace each non-alphanumeric character with underscore
                .replace("_+".toRegex(), "_") // Remove repeated underscores
        }
    }
}
