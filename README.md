# basmaxanae

A Spring Boot web application with a Vaadin UI that accepts font files and produces an STL model containing all glyphs of the font. The application can run as a standalone executable or be deployed as a WAR file to an external Tomcat server.

## Usage

Build the project and run the executable WAR:

```
mvn package
java -jar target/sgv2stl-0.1.0-SNAPSHOT.war
```

Once running, open `http://localhost:8080` in a browser and upload a font to download the generated `font.stl` containing simple extrusions for every glyph. A REST endpoint is also available for automated usage:

```
curl -F file=@DejaVuSans.ttf http://localhost:8080/convert -o font.stl
```

## Tests

```
mvn test
```
