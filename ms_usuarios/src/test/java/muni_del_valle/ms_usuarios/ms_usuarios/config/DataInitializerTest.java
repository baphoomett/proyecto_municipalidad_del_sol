package muni_del_valle.ms_usuarios.ms_usuarios.config;

import muni_del_valle.ms_usuarios.ms_usuarios.model.Role;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void init_cuandoNoExistenRoles_deberiaCrearlos() {
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        dataInitializer.init();

        verify(roleRepository, times(3)).save(any(Role.class));
    }

    @Test
    void init_cuandoTodosLosRolesExisten_noDeberiaCrearlos() {
        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role("EXISTS")));

        dataInitializer.init();

        verify(roleRepository, never()).save(any());
    }

    @Test
    void init_cuandoAlgunosRolesExisten_deberiaCrearSoloLosFaltantes() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER")));
        when(roleRepository.findByName("ROLE_GUEST")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());

        dataInitializer.init();

        verify(roleRepository, times(2)).save(any(Role.class));
    }
}
