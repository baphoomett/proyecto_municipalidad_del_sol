package muni_del_valle.ms_monitoreo.ms_monitoreo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateFocusRequest {
    @NotNull
    private Long reportId;

    @NotNull
    @Size(min = 3)
    private String geometry; // GeoJSON

    @Size(max = 2000)
    private String description;

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getGeometry() { return geometry; }
    public void setGeometry(String geometry) { this.geometry = geometry; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
