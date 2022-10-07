package com.challenge.files.service;

import com.challenge.files.dto.OneRandomLineResponse;
import com.challenge.files.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileInfo uploadFile(MultipartFile file);

    OneRandomLineResponse getOneRandomLineFromPrevFile(boolean extended);

    String getOneRandomLineBackwardsFromAllFiles();

    List<String> getLongest100LinesFromAllFiles();

    List<String> get20LongestLinesFromLastFile();
}
