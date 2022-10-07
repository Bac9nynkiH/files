package com.challenge.files.controller;

import com.challenge.files.entity.FileInfo;
import com.challenge.files.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@AllArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"}, produces = {"application/json"})
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!file.getResource().isReadable())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    @GetMapping(value = "/getRandomFromPrev", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_PLAIN_VALUE  })
    public ResponseEntity<?> getRandomFromPrev(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        MediaType mediaType = headers.getContentType();
        if (MediaType.TEXT_PLAIN.equals(mediaType))
            return ResponseEntity.ok(fileService.getOneRandomLineFromPrevFile(false).getLine());
        if (MediaType.APPLICATION_JSON.equals(mediaType))
            return ResponseEntity.ok(fileService.getOneRandomLineFromPrevFile(true));
        if (MediaType.APPLICATION_XML.equals(mediaType)) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            return new ResponseEntity<>(fileService.getOneRandomLineFromPrevFile(true), httpHeaders,HttpStatus.OK);
        }
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "/getTwentyLongest")
    public ResponseEntity<List<String>> getTwentyLongest() {
        return ResponseEntity.ok(fileService.get20LongestLinesFromLastFile());
    }

    @GetMapping(value = "/getRandomBackwardsLine")
    public ResponseEntity<String> getRandomBackwardsLine() {
        return ResponseEntity.ok(fileService.getOneRandomLineBackwardsFromAllFiles());
    }

    @GetMapping(value = "/getLongestHundredLinesFromAllFiles")
    public ResponseEntity<List<String>> getLongest100LinesFromAllFiles() {
        return ResponseEntity.ok(fileService.getLongest100LinesFromAllFiles());
    }

}
