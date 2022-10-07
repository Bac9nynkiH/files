package com.challenge.files.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.challenge.files.dto.OneRandomLineExtendedResponse;
import com.challenge.files.dto.OneRandomLineResponse;
import com.challenge.files.entity.FileInfo;
import com.challenge.files.entity.LongestLine;
import com.challenge.files.exception.NotFoundException;
import com.challenge.files.exception.ServerException;
import com.challenge.files.repo.FileInfoRepo;
import com.challenge.files.repo.LongestLinesRepo;
import com.challenge.files.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImplementation implements FileService {
    @Value("${AWS.bucket}")
    private String AWSBucket;
    private final AmazonS3 s3Client;
    private final FileInfoRepo fileInfoRepo;
    private final LongestLinesRepo longestLinesRepo;

    @Override
    @Transactional
    public FileInfo uploadFile(MultipartFile multipartFile) {
        File localFile = new File(UUID.randomUUID().toString());
        FileInfo infoTemporaryFile = writeFromMultipartFileToFileAndUpdateLongestLinesIfNeeded(multipartFile, localFile.getName());
        FileInfo fileInfo = saveFileInfo(new FileInfo(multipartFile.getOriginalFilename(), infoTemporaryFile.getRowsCount()));
        s3Client.putObject(
                AWSBucket,
                fileInfo.getCompositeName(),
                localFile
        );
        localFile.delete();
        return fileInfo;
    }

    @Override
    public OneRandomLineResponse getOneRandomLineFromPrevFile(boolean extended) {
        FileInfo fileInfo = fileInfoRepo.findTopByOrderByIdDesc().orElseThrow(() -> new NotFoundException("[getOneRandomLineFromPrevFile] no files were found"));
        BigInteger row = BigDecimal.valueOf(Math.random() * 100).toBigInteger().multiply(fileInfo.getRowsCount().subtract(BigInteger.ONE)).divide(BigInteger.valueOf(100));
        String line = getRowFromFile(fileInfo, row);

        if (extended) {
            return new OneRandomLineExtendedResponse(line, row, fileInfo.getCompositeName().substring(fileInfo.getCompositeName().indexOf("I")+1), findMaximumOccuringChar(line));
        } else {
            return new OneRandomLineResponse(line);
        }
    }

    @Override
    public String getOneRandomLineBackwardsFromAllFiles() {
        FileInfo fileInfo = fileInfoRepo.findTopByOrderByIdDesc().orElseThrow(() -> new NotFoundException("[getOneRandomLineFromPrevFile] no files were found"));
        BigInteger idToBeFound = BigDecimal.valueOf(Math.random() * 100).toBigInteger().multiply(fileInfo.getId()).divide(BigInteger.valueOf(100));
        if(idToBeFound.equals(BigInteger.ZERO))
            idToBeFound = idToBeFound.add(BigInteger.ONE);
        FileInfo fileToSelectRandomRow = findFileInfoById(idToBeFound);
        BigInteger row = BigDecimal.valueOf(Math.random() * 100).toBigInteger().multiply(fileToSelectRandomRow.getRowsCount()).divide(BigInteger.valueOf(100));

        String line = getRowFromFile(fileInfo, row);
        StringBuilder sb = new StringBuilder();
        sb.append(line);

        return sb.reverse().toString();
    }

    @Override
    public List<String> getLongest100LinesFromAllFiles() {
        return findAllLongestLines().stream().map(line -> line.getLine()).collect(Collectors.toList());
    }

    @Override
    public List<String> get20LongestLinesFromLastFile() {
        FileInfo fileInfo = fileInfoRepo.findTopByOrderByIdDesc().orElseThrow(() -> new NotFoundException("[get20LongestLinesFromLastFile] no files were found"));
        S3Object object = s3Client.getObject(new GetObjectRequest(AWSBucket, fileInfo.getCompositeName()));
        InputStream content = object.getObjectContent();
        Scanner s = new Scanner(content);
        List<String> rows = find20LongestLinesWithScanner(s);
        IOUtils.closeQuietly(object, null);
        return rows;
    }

    private String getRowFromFile(FileInfo fileInfo, BigInteger row) {
        S3Object object = s3Client.getObject(new GetObjectRequest(AWSBucket, fileInfo.getCompositeName()));
        String line = null;
        InputStream content = object.getObjectContent();
        Scanner s = new Scanner(content);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(row) <= 0; i = i.add(BigInteger.ONE)) {
            line = s.nextLine();
        }
        IOUtils.closeQuietly(object, null);
        return line;
    }

    private FileInfo writeFromMultipartFileToFileAndUpdateLongestLinesIfNeeded(MultipartFile multipartFile,
                                                                               String fileName) {
        try {
            InputStream multipartFileInputStream = multipartFile.getInputStream();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            Scanner s = new Scanner(multipartFileInputStream);

            List<LongestLine> longestLines = findAllLongestLines();
            longestLines.sort((a, b) -> Integer.compare(a.getLength(), b.getLength()));

            BigInteger rowsCount = BigInteger.ZERO;
            while (s.hasNextLine()) {
                rowsCount = rowsCount.add(BigInteger.ONE);

                String row = s.nextLine();
                writer.write(row);

                if (s.hasNextLine())
                    writer.write("\n");

                if (longestLines.size() < 100) {
                    longestLines.add(new LongestLine(row, row.length()));
                    if (longestLines.size() == 100)
                        longestLines.sort((a, b) -> Integer.compare(a.getLength(), b.getLength()));

                    continue;
                }

                if (longestLines.get(0).getLength() < row.length()) {
                    longestLines.set(0, longestLines.get(0).updateData(row, row.length()));
                    longestLines.sort((a, b) -> Integer.compare(a.getLength(), b.getLength()));
                }


            }
            saveLongestLines(longestLines);
            IOUtils.closeQuietly(multipartFileInputStream, null);
            writer.close();
            return new FileInfo(fileName, rowsCount);
        } catch (IOException e) {
            log.error("error reading or writing multipart file : " + e);
            throw new ServerException("error reading or saving file");
        }
    }

    private List<LongestLine> findAllLongestLines() {
        try {
            return longestLinesRepo.findAll();
        } catch (Exception e) {
            log.error("[findAllLongestLines] error finding: " + e.getMessage());
            throw new ServerException("unexpectedError");
        }
    }

    private FileInfo findFileInfoById(BigInteger id) {
        try {
            return fileInfoRepo.findById(id).orElseThrow(() -> new NotFoundException("[findFileInfoById] entity with id: " + id + " does not exist"));
        } catch (Exception e) {
            log.error("[findFileInfoById] unexpected error: " + e.getMessage());
            throw new ServerException("unexpected error");
        }
    }

    private FileInfo saveFileInfo(FileInfo fileInfo) {
        try {
            return fileInfoRepo.save(fileInfo);
        } catch (Exception e) {
            log.error("[saveFileInfo] error saving to db: " + e.getMessage());
            throw new ServerException("Unexpected error");
        }
    }

    private List<LongestLine> saveLongestLines(List<LongestLine> longestLines) {
        try {
            return longestLinesRepo.saveAll(longestLines);
        } catch (Exception e) {
            log.error("[saveLongestLines] error saving to db: " + e.getMessage());
            throw new ServerException("Unexpected error");
        }
    }

    private List<String> find20LongestLinesWithScanner(Scanner scannerWithStream) {
        ArrayList<String> rows = new ArrayList<String>(20);
        while (scannerWithStream.hasNextLine()) {
            if (rows.size() < 20) {
                rows.add(scannerWithStream.next());
                if (rows.size() == 20)
                    rows.sort((a, b) -> Integer.compare(a.length(), b.length()));
                continue;
            }
            String row = scannerWithStream.nextLine();
            if (rows.get(0).length() < row.length()) {
                rows.set(0, row);
                rows.sort((a, b) -> Integer.compare(a.length(), b.length()));
            }
        }
        return rows;
    }

    private Character findMaximumOccuringChar(String str) {
        if (null == str)
            return null;
        if (str.isEmpty())
            return null;
        return str.chars()
                .mapToObj(x -> (char) x)
                .collect(groupingBy(x -> x, counting()))
                .entrySet().stream()
                .max(comparingByValue())
                .get()
                .getKey();
    }
}
