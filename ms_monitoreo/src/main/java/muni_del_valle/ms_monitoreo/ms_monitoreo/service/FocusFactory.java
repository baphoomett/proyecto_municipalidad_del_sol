package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Severity;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

public class FocusFactory {

    public static Focus createFocus(CreateFocusRequest req) {
        Focus f = new Focus();
        f.setReportId(req.getReportId());
        try {
            if (req.getGeometry() != null) {
                WKTReader rdr = new WKTReader();
                Geometry g = rdr.read(req.getGeometry());
                f.setGeometry(g);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid geometry WKT: " + ex.getMessage());
        }
        f.setDescription(req.getDescription());
        try {
            f.setSeverity(Severity.valueOf(req.getSeverity()));
        } catch (Exception ex) {
            f.setSeverity(Severity.MEDIUM);
        }
        return f;
    }
}
