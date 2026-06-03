package muni_del_valle.ms_monitoreo.ms_monitoreo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "brigades")
public class Brigade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean active = true;

    @Column(length = 2000)
    private String areaGeoJson; // optional area or location represented as GeoJSON

    public Brigade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getAreaGeoJson() { return areaGeoJson; }
    public void setAreaGeoJson(String areaGeoJson) { this.areaGeoJson = areaGeoJson; }
}
