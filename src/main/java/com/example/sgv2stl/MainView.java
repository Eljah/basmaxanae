package com.example.sgv2stl;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Route("")
public class MainView extends VerticalLayout {

    @Autowired
    public MainView(FontToStlService service) {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".ttf", ".otf", ".woff", ".woff2", ".svg", ".eot");
        Anchor download = new Anchor();
        download.setText("Download STL");
        download.getElement().setAttribute("download", true);
        download.getElement().setVisible(false);
        add(upload, download);

        upload.addSucceededListener(event -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                String stl = service.convert(bytes, 5);
                StreamResource resource = new StreamResource("font.stl",
                        () -> new ByteArrayInputStream(stl.getBytes(StandardCharsets.UTF_8)));
                download.setHref(resource);
                download.getElement().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
