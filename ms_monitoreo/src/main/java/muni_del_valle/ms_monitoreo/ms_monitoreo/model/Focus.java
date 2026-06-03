package muni_del_valle.ms_monitoreo.ms_monitoreo.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import java.time.Instant;

@Entity
@Table(name = "focus")
public class Focus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId; // reference to ms_reportes.Report id

    @Column(columnDefinition = "geometry")
    private Geometry geometry; // PostGIS geometry

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private FocusStatus status = FocusStatus.NEW;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.MEDIUM;

    private Instant createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Focus() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Geometry getGeometry() { return geometry; }
    public void setGeometry(Geometry geometry) { this.geometry = geometry; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FocusStatus getStatus() { return status; }
    public void setStatus(FocusStatus status) { this.status = status; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
