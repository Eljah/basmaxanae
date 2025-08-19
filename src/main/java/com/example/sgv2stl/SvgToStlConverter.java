package com.example.sgv2stl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Simple converter that extrudes a rectangle defined in an SVG file into a 3D block written as an STL file.
 * The implementation is intentionally minimal and supports only <rect> elements.
 */
public class SvgToStlConverter {

    /**
     * Converts the first <rect> element found in the SVG content into a rectangular prism and returns an ASCII STL string.
     *
     * @param svgContent SVG document content
     * @param depth      depth of the extrusion along the Z axis
     * @return ASCII STL representation
     */
    public String convertRectangleSvgToStl(String svgContent, double depth) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(svgContent.getBytes(StandardCharsets.UTF_8)));
        NodeList rects = doc.getElementsByTagName("rect");
        if (rects.getLength() == 0) {
            throw new IllegalArgumentException("SVG does not contain <rect> elements");
        }
        Element rect = (Element) rects.item(0);
        double x = parseDouble(rect.getAttribute("x"));
        double y = parseDouble(rect.getAttribute("y"));
        double width = parseDouble(rect.getAttribute("width"));
        double height = parseDouble(rect.getAttribute("height"));
        return buildStlForRectangle(x, y, width, height, depth);
    }

    /**
     * Reads an SVG file and writes the resulting STL file using {@link #convertRectangleSvgToStl(String, double)}.
     */
    public void convert(File svgFile, File stlFile, double depth) throws Exception {
        String content = Files.readString(svgFile.toPath());
        String stl = convertRectangleSvgToStl(content, depth);
        Files.writeString(stlFile.toPath(), stl, StandardCharsets.UTF_8);
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    private String buildStlForRectangle(double x, double y, double width, double height, double depth) {
        double x2 = x + width;
        double y2 = y + height;
        double z = depth;
        StringBuilder sb = new StringBuilder();
        sb.append("solid rectangle\n");

        // Bottom face
        appendFacet(sb, 0, 0, -1, x, y, 0, x2, y, 0, x2, y2, 0);
        appendFacet(sb, 0, 0, -1, x, y, 0, x2, y2, 0, x, y2, 0);

        // Top face
        appendFacet(sb, 0, 0, 1, x, y, z, x2, y2, z, x2, y, z);
        appendFacet(sb, 0, 0, 1, x, y, z, x, y2, z, x2, y2, z);

        // Left face
        appendFacet(sb, -1, 0, 0, x, y, 0, x, y2, z, x, y, z);
        appendFacet(sb, -1, 0, 0, x, y, 0, x, y2, 0, x, y2, z);

        // Right face
        appendFacet(sb, 1, 0, 0, x2, y, 0, x2, y, z, x2, y2, z);
        appendFacet(sb, 1, 0, 0, x2, y, 0, x2, y2, z, x2, y2, 0);

        // Back face
        appendFacet(sb, 0, -1, 0, x, y, 0, x2, y, z, x2, y, 0);
        appendFacet(sb, 0, -1, 0, x, y, 0, x, y, z, x2, y, z);

        // Front face
        appendFacet(sb, 0, 1, 0, x, y2, 0, x2, y2, 0, x2, y2, z);
        appendFacet(sb, 0, 1, 0, x, y2, 0, x2, y2, z, x, y2, z);

        sb.append("endsolid rectangle\n");
        return sb.toString();
    }

    private void appendFacet(StringBuilder sb, double nx, double ny, double nz,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             double x3, double y3, double z3) {
        sb.append("  facet normal ").append(nx).append(' ').append(ny).append(' ').append(nz).append("\n");
        sb.append("    outer loop\n");
        sb.append("      vertex ").append(x1).append(' ').append(y1).append(' ').append(z1).append("\n");
        sb.append("      vertex ").append(x2).append(' ').append(y2).append(' ').append(z2).append("\n");
        sb.append("      vertex ").append(x3).append(' ').append(y3).append(' ').append(z3).append("\n");
        sb.append("    endloop\n");
        sb.append("  endfacet\n");
    }
}
