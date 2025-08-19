package com.example.sgv2stl;

import org.springframework.stereotype.Service;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FontToStlService {

    public String convert(byte[] fontBytes, double depth) throws Exception {
        Font font = loadFont(fontBytes);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        StringBuilder sb = new StringBuilder();
        sb.append("solid font\n");
        double xOffset = 0;

        for (int glyph = 1; glyph < font.getNumGlyphs(); glyph++) {
            GlyphVector gv = font.createGlyphVector(frc, new int[]{glyph});
            Shape outline = gv.getGlyphOutline(0);
            AffineTransform at = AffineTransform.getTranslateInstance(xOffset - outline.getBounds2D().getX(), -outline.getBounds2D().getY());
            Shape shifted = at.createTransformedShape(outline);
            buildShape(sb, shifted, depth);
            xOffset += shifted.getBounds2D().getWidth() + 5; // simple spacing between glyphs
        }

        sb.append("endsolid font\n");
        return sb.toString();
    }

    private Font loadFont(byte[] fontBytes) throws Exception {
        try (InputStream in = new ByteArrayInputStream(fontBytes)) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        }
    }

    private void buildShape(StringBuilder sb, Shape shape, double depth) {
        List<List<Point2D.Double>> polygons = toPolygons(shape);
        for (List<Point2D.Double> poly : polygons) {
            if (poly.size() < 3) continue;
            addPolygon(sb, poly, depth);
        }
    }

    private List<List<Point2D.Double>> toPolygons(Shape shape) {
        List<List<Point2D.Double>> result = new ArrayList<>();
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.5);
        double[] coords = new double[6];
        List<Point2D.Double> current = new ArrayList<>();
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO -> {
                    if (!current.isEmpty()) {
                        result.add(current);
                        current = new ArrayList<>();
                    }
                    current.add(new Point2D.Double(coords[0], coords[1]));
                }
                case PathIterator.SEG_LINETO -> current.add(new Point2D.Double(coords[0], coords[1]));
                case PathIterator.SEG_CLOSE -> {
                    result.add(current);
                    current = new ArrayList<>();
                }
            }
            it.next();
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    private void addPolygon(StringBuilder sb, List<Point2D.Double> poly, double depth) {
        List<Triangle> triangles = triangulate(poly);
        for (Triangle t : triangles) {
            // bottom
            appendFacet(sb, 0, 0, -1,
                    t.a.x, t.a.y, 0,
                    t.b.x, t.b.y, 0,
                    t.c.x, t.c.y, 0);
            // top (reverse order)
            appendFacet(sb, 0, 0, 1,
                    t.a.x, t.a.y, depth,
                    t.c.x, t.c.y, depth,
                    t.b.x, t.b.y, depth);
        }

        for (int i = 0; i < poly.size(); i++) {
            Point2D.Double p1 = poly.get(i);
            Point2D.Double p2 = poly.get((i + 1) % poly.size());
            // side quad split into two triangles
            appendFacet(sb, 0, 0, 0,
                    p1.x, p1.y, 0,
                    p2.x, p2.y, 0,
                    p2.x, p2.y, depth);
            appendFacet(sb, 0, 0, 0,
                    p1.x, p1.y, 0,
                    p2.x, p2.y, depth,
                    p1.x, p1.y, depth);
        }
    }

    private List<Triangle> triangulate(List<Point2D.Double> poly) {
        List<Point2D.Double> points = new ArrayList<>(poly);
        List<Triangle> triangles = new ArrayList<>();
        if (points.size() < 3) return triangles;
        List<Point2D.Double> verts = new ArrayList<>(points);
        while (verts.size() >= 3) {
            boolean earFound = false;
            int n = verts.size();
            for (int i = 0; i < n; i++) {
                Point2D.Double prev = verts.get((i + n - 1) % n);
                Point2D.Double curr = verts.get(i);
                Point2D.Double next = verts.get((i + 1) % n);
                if (cross(prev, curr, next) <= 0) {
                    continue; // reflex
                }
                Triangle ear = new Triangle(prev, curr, next);
                boolean contains = false;
                for (Point2D.Double p : verts) {
                    if (p == prev || p == curr || p == next) continue;
                    if (ear.contains(p)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    triangles.add(ear);
                    verts.remove(i);
                    earFound = true;
                    break;
                }
            }
            if (!earFound) {
                // polygon not simple, abort to avoid infinite loop
                break;
            }
        }
        return triangles;
    }

    private double cross(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
        double abx = b.x - a.x;
        double aby = b.y - a.y;
        double bcx = c.x - b.x;
        double bcy = c.y - b.y;
        return abx * bcy - aby * bcx;
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

    private static class Triangle {
        final Point2D.Double a;
        final Point2D.Double b;
        final Point2D.Double c;

        Triangle(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        boolean contains(Point2D.Double p) {
            double area = cross(a, b, c);
            double area1 = cross(p, b, c);
            double area2 = cross(a, p, c);
            double area3 = cross(a, b, p);
            boolean hasNeg = (area1 < 0) || (area2 < 0) || (area3 < 0);
            boolean hasPos = (area1 > 0) || (area2 > 0) || (area3 > 0);
            return area > 0 ? !(hasNeg && hasPos) : (hasNeg && hasPos);
        }
    }
}
