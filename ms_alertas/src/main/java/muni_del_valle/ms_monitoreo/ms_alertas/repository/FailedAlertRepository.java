package muni_del_valle.ms_monitoreo.ms_alertas.repository;

import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedAlertRepository extends JpaRepository<FailedAlert, Long> {
	java.util.List<FailedAlert> findByAttemptsLessThan(int maxAttempts);
}
