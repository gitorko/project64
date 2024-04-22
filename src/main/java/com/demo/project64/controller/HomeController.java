package com.demo.project64.controller;

import java.util.List;
import java.util.Optional;

import com.demo.project64.domain.Customer;
import com.demo.project64.domain.DownloadFile;
import com.demo.project64.service.CsvService;
import com.demo.project64.repositoryservice.CustomerReactiveRepoService;
import com.demo.project64.repositoryservice.DownloadFileReactiveRepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final CustomerReactiveRepoService customerReactiveService;
    private final CsvService csvService;
    private final DownloadFileReactiveRepoService downloadFileReactiveRepoService;

    @GetMapping("/customers")
    public Flux<Customer> getAllCustomer() {
        return customerReactiveService.findAll();
    }

    @GetMapping("/customer/{customerId}")
    public Mono<Optional<Customer>> findById(@PathVariable Long customerId) {
        return customerReactiveService.findById(customerId);
    }

    @PostMapping(value = "/customer")
    public Mono<Customer> save(@RequestBody Customer customer) {
        return customerReactiveService.save(customer);
    }

    @GetMapping("/customer")
    public Mono<List<Customer>> findById(@RequestParam String name, @RequestParam Integer age) {
        return customerReactiveService.findByNameAndAge(name, age);
    }

    @GetMapping("/csv")
    public Mono<DownloadFile> generateCsvFile() {
        return csvService.generateCsvFile();
    }

    @GetMapping("/downloads")
    public Flux<DownloadFile> findAllDownloads() {
        return downloadFileReactiveRepoService.findAll();
    }

    @GetMapping(path = "/download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Mono<ResponseEntity<Resource>> downloadCsvFile(@PathVariable("id") Long id) {
        return csvService.getFileResourcePath(id)
                .filter(response -> csvService.isFileExists(response))
                .flatMap(s -> {
                    Resource resource = new FileSystemResource(s);
                    return Mono.just(ResponseEntity.ok()
                            .cacheControl(CacheControl.noCache())
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            .body(resource));

                }).defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
