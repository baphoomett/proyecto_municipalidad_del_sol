package muni_del_valle.ms_reportes.ms_reportes.repository;

import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

void deleteByReportId(Long reportId);

}
