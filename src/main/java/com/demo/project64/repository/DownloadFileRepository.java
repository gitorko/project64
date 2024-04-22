package com.demo.project64.repository;

import com.demo.project64.domain.DownloadFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadFileRepository extends JpaRepository<DownloadFile, Long> {
}
