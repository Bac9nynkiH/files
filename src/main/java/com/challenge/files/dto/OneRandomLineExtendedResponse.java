package com.challenge.files.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class OneRandomLineExtendedResponse extends OneRandomLineResponse {
    private BigInteger lineNumber;
    private String fileName;
    private Character mostOccurrences;

    public OneRandomLineExtendedResponse(String line, BigInteger lineNumber, String fileName, Character mostOccurrences) {
        super(line);
        this.lineNumber = lineNumber;
        this.fileName = fileName;
        this.mostOccurrences = mostOccurrences;
    }
}
