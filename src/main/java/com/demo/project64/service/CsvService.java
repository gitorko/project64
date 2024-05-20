package com.demo.project64.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.demo.project64.domain.Customer;
import com.demo.project64.domain.DownloadFile;
import com.demo.project64.domain.FileWriteStatus;
import com.demo.project64.repositoryservice.CustomerReactiveRepoService;
import com.demo.project64.repositoryservice.DownloadFileReactiveRepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvService {

    private static final String CSV_COL_SEPARATOR = ",";
    private static final String CSV_LINE_SEPARATOR = "\n";
    private static final String FILE_NAME_FORMAT = "customer-%s.csv";
    private static final int downloadPageSize = 5000;
    private static final String CSV_HEADER_LINE = "Id,Name,Age\n";
    private final DownloadFileReactiveRepoService downloadFileReactiveService;
    private final CustomerReactiveRepoService customerReactiveRepoService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private final DownloadFileReactiveRepoService downloadFileReactiveRepoService;
    private String filePath = "/tmp/";

    public Mono<DownloadFile> generateCsvFile() {
        LocalDateTime localDate = LocalDateTime.now();
        DownloadFile downloadFile = DownloadFile.builder()
                .fileName(String.format(FILE_NAME_FORMAT, localDate.format(formatter)))
                .filePath(filePath)
                .fileStatus(FileWriteStatus.IN_PROGRESS)
                .createdDate(new Date())
                .build();
        return downloadFileReactiveService.save(downloadFile)
                .flatMap(e -> {
                    generate(downloadFile)
                            .subscribeOn(Schedulers.single())
                            .subscribe();
                    return Mono.just(e);
                });
    }

    private Mono<Void> generate(DownloadFile downloadFile) {
        log.info("Generating csv file!");
        Pageable pageable = PageRequest.of(0, downloadPageSize);
        return customerReactiveRepoService.findAll(pageable)
                .flatMap(p -> this.writeHeader(downloadFile, p))
                .flatMap(p -> this.writeContentToFile(downloadFile, p))
                .flatMap(customerPage -> {
                    return Flux.range(1, customerPage.getTotalPages() - 1)
                            .flatMapSequential(index -> {
                                log.info("Page Index: {}", index);
                                Pageable page = PageRequest.of(index, downloadPageSize);
                                return customerReactiveRepoService.findAll(page);
                            }, 1).flatMapSequential(pe -> {
                                return this.writeContentToFile(downloadFile, pe);
                            }, 1).then();
                })
                .then(Mono.just(downloadFile.getId()))
                .flatMap(f -> updateStatus(f, FileWriteStatus.COMPLETED))
                .onErrorResume(ex -> {
                    log.error("Download file failed! {} for {}", ex.getMessage(), downloadFile.getFileName());
                    return Mono.just(downloadFile.getId())
                            .flatMap(f -> updateStatus(f, FileWriteStatus.FAILED));
                }).doOnError(ex -> {
                    log.error("Download file status update failed! {} for {}", ex.getMessage(), downloadFile.getFileName());
                }).doOnSuccess((e) -> {
                    log.info("Download file generated successfully for {}", downloadFile.getFileName());
                })
                .then();
    }

    /**
     * Writes the header for csv file in create mode.
     */
    private Mono<Page<Customer>> writeHeader(DownloadFile downloadFile, Page<Customer> customerPage) {
        return Mono.fromRunnable(() -> {
            Path fileName = Path.of(downloadFile.getFilePath() + downloadFile.getFileName());
            try {
                Files.writeString(fileName, CSV_HEADER_LINE, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write header to csv file " + ex.getMessage());
            }
        }).thenReturn(customerPage);
    }

    private Mono<Page<Customer>> writeContentToFile(DownloadFile downloadFile, Page<Customer> customerPage) {
        return Mono.fromRunnable(() -> {
            StringBuilder sb = new StringBuilder();
            for (Customer a : customerPage) {
                sb.append(a.getId());
                sb.append(CSV_COL_SEPARATOR);
                sb.append(a.getName());
                sb.append(CSV_COL_SEPARATOR);
                sb.append(a.getAge());
                sb.append(CSV_LINE_SEPARATOR);
            }
            Path fileName = Path.of(downloadFile.getFilePath() + downloadFile.getFileName());
            try {
                Files.writeString(fileName, sb.toString(), StandardOpenOption.APPEND);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write content to csv file " + ex.getMessage());
            }
        }).thenReturn(customerPage);
    }

    public Mono<DownloadFile> updateStatus(Long id, FileWriteStatus status) {
        return Mono.just(id)
                .flatMap(downloadFileReactiveService::findById)
                .map(f -> {
                    if (f.isPresent()) {
                        return f.get();
                    } else {
                        throw new IllegalStateException("File entity missing " + id);
                    }
                })
                .map(f -> {
                    f.setFileStatus(status);
                    return f;
                })
                .flatMap(downloadFileReactiveService::save);
    }

    public Mono<String> getFileResourcePath(long fileId) {
        return Mono.just(fileId)
                .flatMap(f -> downloadFileReactiveRepoService.findById(fileId))
                .map(f -> {
                    if (f.isPresent()) {
                        return f.get();
                    } else {
                        throw new IllegalStateException("File entity missing " + fileId);
                    }
                })
                .map(f -> f.getFilePath() + f.getFileName());
    }

    public boolean isFileExists(String response) {
        Resource resource = new FileSystemResource(response);
        return resource.exists();
    }
}
