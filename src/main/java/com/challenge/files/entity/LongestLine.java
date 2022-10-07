package com.challenge.files.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "longest_lines")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class LongestLine {
    public LongestLine(String line, int length) {
        this.line = line;
        this.length = length;
    }

    @Id
    @SequenceGenerator(name = "longestLineSequence", sequenceName = "longest_lines_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "longestLineSequence")
    private BigInteger id;

    @Column(name = "line")
    private String line;

    @Column(name = "length")
    private int length;

    public LongestLine updateData(String line, int length) {
        this.line = line;
        this.length = length;
        return this;
    }
}
