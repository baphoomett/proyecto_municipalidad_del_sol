package muni_del_valle.ms_monitoreo.ms_alertas.repository;

import muni_del_valle.ms_monitoreo.ms_alertas.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AlertRepository extends JpaRepository<Alert, Long>
    {
@Transactional
void deleteByReportId(Long reportId);
}

