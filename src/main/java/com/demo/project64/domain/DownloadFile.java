package com.demo.project64.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "download_file")
@Builder
public class DownloadFile {

    public static final String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "file_name", nullable = false, updatable = false)
    private String fileName;

    @NotNull
    @Column(name = "file_path", nullable = false, updatable = false)
    @JsonIgnore
    private String filePath;

    @NotNull
    @Column(name = "file_status", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private FileWriteStatus fileStatus;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO8601_DATE_PATTERN)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;
}
