package muni_del_valle.bff.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReportDto {
    private String reporterEmail;
    private Double latitude;
    private Double longitude;
    private String description;
    private List<String> mediaUrls;
}