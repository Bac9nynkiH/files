package com.challenge.files.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;


@Entity
@Table(name = "file_info")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FileInfo {
    public FileInfo(String fileName) {
        this.fileName = fileName;
    }

    public FileInfo(String fileName, BigInteger rowsCount) {
        this.fileName = fileName;
        this.rowsCount = rowsCount;
    }

    @Id
    @SequenceGenerator(name = "fileInfoSequence", sequenceName = "file_info_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fileInfoSequence")
    private BigInteger id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "rows_count")
    private BigInteger rowsCount;

    public String getCompositeName() {
        return id + "I" + fileName;
    }
}
