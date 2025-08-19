package com.example.sgv2stl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SvgToStlConverterTest {

    @Test
    void convertsRectangleSvg() throws Exception {
        String svg = "<svg height='100' width='100'><rect x='0' y='0' width='10' height='20'/></svg>";
        SvgToStlConverter converter = new SvgToStlConverter();
        String stl = converter.convertRectangleSvgToStl(svg, 5);
        assertTrue(stl.contains("solid rectangle"));
        assertTrue(stl.contains("endsolid rectangle"));
    }
}
