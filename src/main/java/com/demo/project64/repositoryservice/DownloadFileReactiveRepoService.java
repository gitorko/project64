package com.demo.project64.repositoryservice;

import com.demo.project64.domain.DownloadFile;
import com.demo.project64.repository.DownloadFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadFileReactiveRepoService extends AbstractReactiveRepoService<DownloadFile> {

    private final DownloadFileRepository downloadFileRepository;

    @Override
    protected JpaRepository getRepository() {
        return downloadFileRepository;
    }

}
