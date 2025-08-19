package com.example.sgv2stl;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FontToStlServiceTest {

    @Test
    void convertsFont() throws Exception {
        Path fontPath = Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");
        Assumptions.assumeTrue(Files.exists(fontPath), "System font not available");
        byte[] font = Files.readAllBytes(fontPath);
        FontToStlService service = new FontToStlService();
        String stl = service.convert(font, 5);
        assertTrue(stl.startsWith("solid font"));
        assertTrue(stl.contains("facet"));
        assertTrue(stl.endsWith("endsolid font\n"));
    }
}
