package br.com.sena.datascale.entities;

import br.com.sena.datascale.entities.enums.IngestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingestion_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IngestionStatus status;

    @Column(name = "total_lines_read", nullable = false)
    @Builder.Default
    private Long totalLinesRead = 0L;

    @Column(name = "lines_processed", nullable = false)
    @Builder.Default
    private Long linesProcessed = 0L;

    @Column(name = "lines_with_error", nullable = false)
    @Builder.Default
    private Long linesWithError = 0L;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = IngestionStatus.RECEIVED;
        }
    }
}
