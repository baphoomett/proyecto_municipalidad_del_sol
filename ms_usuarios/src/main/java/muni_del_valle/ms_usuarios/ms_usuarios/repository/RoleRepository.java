package muni_del_valle.ms_usuarios.ms_usuarios.repository;

import muni_del_valle.ms_usuarios.ms_usuarios.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
