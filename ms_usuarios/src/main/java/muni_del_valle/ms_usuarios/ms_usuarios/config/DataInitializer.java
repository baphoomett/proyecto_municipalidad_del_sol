package muni_del_valle.ms_usuarios.ms_usuarios.config;

import jakarta.annotation.PostConstruct;
import muni_del_valle.ms_usuarios.ms_usuarios.model.Role;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (roleRepository.findByName("ROLE_GUEST").isEmpty()) {
            roleRepository.save(new Role("ROLE_GUEST"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
    }
}
