package muni_del_valle.ms_reportes.ms_reportes.dto;

public class CreateEventRequest {
    private String type; // CREATED, DISPATCHED, CLOSED
    private String payload;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
