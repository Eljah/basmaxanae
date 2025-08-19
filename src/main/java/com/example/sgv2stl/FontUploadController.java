package com.example.sgv2stl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FontUploadController {

    private final FontToStlService service;

    public FontUploadController(FontToStlService service) {
        this.service = service;
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convert(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "depth", defaultValue = "5") double depth) throws Exception {
        String stl = service.convert(file.getBytes(), depth);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"font.stl\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stl.getBytes());
    }
}
