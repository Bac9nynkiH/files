package com.challenge.files.repo;

import com.challenge.files.entity.LongestLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LongestLinesRepo extends JpaRepository<LongestLine, Integer> {
    List<LongestLine> findAll();

}
