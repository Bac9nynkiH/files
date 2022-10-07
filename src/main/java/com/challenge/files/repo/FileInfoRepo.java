package com.challenge.files.repo;

import com.challenge.files.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface FileInfoRepo extends JpaRepository<FileInfo, BigInteger> {
    Optional<FileInfo> findTopByOrderByIdDesc();
}
