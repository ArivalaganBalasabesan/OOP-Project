package com.eventmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for file operations
 * Handles reading and writing to text files for data persistence
 */
@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final String DATA_DIR = "data";

    /**
     * Constructor to create data directory if it doesn't exist
     * Throws a RuntimeException if directory creation fails
     */
    public FileService() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                logger.info("Created data directory: {}", dataPath.toAbsolutePath());
            } else {
                logger.info("Data directory already exists: {}", dataPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create data directory: {}", DATA_DIR, e);
            throw new RuntimeException("Failed to create data directory: " + DATA_DIR, e);
        }
    }

    /**
     * Read content from a file
     * @param filename The name of the file to read
     * @return List of strings, each representing a line in the file
     */
    public List<String> readFile(String filename) throws IOException {
        String cleanFilename = filename.startsWith("data/") ? filename.substring(5) : filename;
        Path filePath = Paths.get(DATA_DIR, cleanFilename);
        logger.debug("Attempting to read file: {}", filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            logger.info("File does not exist, creating: {}", filePath.toAbsolutePath());
            try {
                Files.createFile(filePath);
                return new ArrayList<>();
            } catch (IOException e) {
                logger.error("Failed to create file: {}", filePath.toAbsolutePath(), e);
                throw new IOException("Failed to create file: " + filePath.toAbsolutePath(), e);
            }
        }

        if (!Files.isReadable(filePath)) {
            logger.error("File is not readable: {}", filePath.toAbsolutePath());
            throw new IOException("File is not readable: " + filePath.toAbsolutePath());
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            logger.debug("Read {} lines from file: {}", lines.size(), lines);
            return lines;
        } catch (IOException e) {
            logger.error("Error reading file: {}", filePath.toAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Write content to a file
     * @param filename The name of the file to write
     * @param lines List of strings to write to the file
     */
    public void writeFile(String filename, List<String> lines) throws IOException {
        String cleanFilename = filename.startsWith("data/") ? filename.substring(5) : filename;
        Path filePath = Paths.get(DATA_DIR, cleanFilename);
        logger.debug("Attempting to write to file: {}", filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            try {
                logger.info("File does not exist, creating: {}", filePath.toAbsolutePath());
                Files.createFile(filePath);
            } catch (IOException e) {
                logger.error("Failed to create file: {}", filePath.toAbsolutePath(), e);
                throw new IOException("Failed to create file: " + filePath.toAbsolutePath(), e);
            }
        }

        if (!Files.isWritable(filePath)) {
            logger.error("File is not writable: {}", filePath.toAbsolutePath());
            throw new IOException("File is not writable: " + filePath.toAbsolutePath());
        }

        try {
            logger.debug("Writing {} lines to file: {}", lines.size(), lines);
            Files.write(filePath, lines);
            logger.debug("Successfully wrote to file: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing to file: {}", filePath.toAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Append a line to a file
     * @param filename The name of the file to append to
     * @param line The string to append
     */
    public void appendToFile(String filename, String line) throws IOException {
        String cleanFilename = filename.startsWith("data/") ? filename.substring(5) : filename;
        Path filePath = Paths.get(DATA_DIR, cleanFilename);
        logger.debug("Attempting to append to file: {}", filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            try {
                logger.info("File does not exist, creating: {}", filePath.toAbsolutePath());
                Files.createFile(filePath);
            } catch (IOException e) {
                logger.error("Failed to create file: {}", filePath.toAbsolutePath(), e);
                throw new IOException("Failed to create file: " + filePath.toAbsolutePath(), e);
            }
        }

        if (!Files.isWritable(filePath)) {
            logger.error("File is not writable: {}", filePath.toAbsolutePath());
            throw new IOException("File is not writable: " + filePath.toAbsolutePath());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            writer.write(line);
            writer.newLine();
            logger.debug("Appended line to file: {}", line);
        } catch (IOException e) {
            logger.error("Error appending to file: {}", filePath.toAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Delete a file
     * @param filename The name of the file to delete
     */
    public boolean deleteFile(String filename) throws IOException {
        String cleanFilename = filename.startsWith("data/") ? filename.substring(5) : filename;
        Path filePath = Paths.get(DATA_DIR, cleanFilename);
        logger.debug("Attempting to delete file: {}", filePath.toAbsolutePath());

        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                logger.info("Deleted file: {}", filePath.toAbsolutePath());
                return true;
            } catch (IOException e) {
                logger.error("Failed to delete file: {}", filePath.toAbsolutePath(), e);
                throw e;
            }
        }

        logger.warn("File does not exist to delete: {}", filePath.toAbsolutePath());
        return false;
    }

    /**
     * Check if a file exists
     * @param filename The name of the file to check
     */
    public boolean fileExists(String filename) {
        String cleanFilename = filename.startsWith("data/") ? filename.substring(5) : filename;
        Path filePath = Paths.get(DATA_DIR, cleanFilename);
        boolean exists = Files.exists(filePath);
        logger.debug("Checking if file exists: {} -> {}", filePath.toAbsolutePath(), exists);
        return exists;
    }
}