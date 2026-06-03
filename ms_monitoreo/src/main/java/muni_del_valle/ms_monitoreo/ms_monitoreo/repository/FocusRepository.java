package muni_del_valle.ms_monitoreo.ms_monitoreo.repository;

import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.FocusStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusRepository extends JpaRepository<Focus, Long> {
    Page<Focus> findByStatus(FocusStatus status, Pageable pageable);
}
