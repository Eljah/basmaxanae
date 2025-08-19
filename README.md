# basmaxanae

A small Java library built with Maven that converts simple SVG rectangle shapes into STL files by extrusion.

## Usage

```java
SvgToStlConverter converter = new SvgToStlConverter();
String svg = "<svg height='100' width='100'><rect x='0' y='0' width='10' height='20'/></svg>";
String stl = converter.convertRectangleSvgToStl(svg, 5);
```

The resulting string contains an ASCII STL representation of a rectangular prism extruded to a depth of 5 units.

## Build

```
mvn test
```
