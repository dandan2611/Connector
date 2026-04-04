package fr.codinbox.connector.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void getExtension_normalFile() {
        assertEquals("yml", FileUtils.getExtension("config.yml"));
    }

    @Test
    void getExtension_multipleDots() {
        assertEquals("properties", FileUtils.getExtension("path/to/my.config.properties"));
    }

    @Test
    void getExtension_noExtension() {
        assertNull(FileUtils.getExtension("Makefile"));
    }

    @Test
    void getExtension_endsWithDot() {
        assertEquals("", FileUtils.getExtension("file."));
    }

    @Test
    void getExtension_pathWithDirectories() {
        assertEquals("yaml", FileUtils.getExtension("/etc/connector/redis.yaml"));
    }
}
