package muni_del_valle.ms_monitoreo.ms_alertas.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "failed_alert")
public class FailedAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(length = 1000)
    private String error;

    private int attempts = 0;

    private Instant createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
