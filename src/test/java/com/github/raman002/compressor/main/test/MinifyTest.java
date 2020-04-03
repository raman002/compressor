package com.github.raman002.compressor.main.test;

import com.github.raman002.compressor.main.Minify;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test cases to validate the compression and decompression of js and csss
 * files.
 *
 * @author Rewatiraman Singh Chandrol
 */

@TestMethodOrder(OrderAnnotation.class)
public class MinifyTest
{
    @Test
    @DisplayName("Validate compression")
    @Order(1)
    void validateFileScanning()
    {
        assertDoesNotThrow(() -> Minify.main(null));
    }

    @Test
    @DisplayName("Validate decompression")
    @Order(2)
    void validateFileDecompression()
    {
        assertDoesNotThrow(() -> Minify.main(new String[] {"revertFiles"}));
    }
}
