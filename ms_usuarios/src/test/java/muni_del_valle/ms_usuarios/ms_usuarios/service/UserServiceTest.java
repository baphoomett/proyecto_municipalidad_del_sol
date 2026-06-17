package muni_del_valle.ms_usuarios.ms_usuarios.service;

import muni_del_valle.ms_usuarios.ms_usuarios.model.Role;
import muni_del_valle.ms_usuarios.ms_usuarios.model.User;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.RoleRepository;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_usuarioExistente_deberiaRetornarUserDetailsConSusRoles() {
        User user = new User();
        user.setEmail("admin@municipalidad.cl");
        user.setPassword("hashed-password");
        user.setRoles(new HashSet<>(java.util.List.of(new Role("ROLE_ADMIN"))));

        when(userRepository.findByEmail("admin@municipalidad.cl")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("admin@municipalidad.cl");

        assertThat(result.getUsername()).isEqualTo("admin@municipalidad.cl");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_usuarioInexistente_deberiaLanzarUsernameNotFoundException() {
        when(userRepository.findByEmail("noexiste@municipalidad.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("noexiste@municipalidad.cl"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void createUser_conRolExistente_deberiaReutilizarElRolYGuardarElUsuarioConPasswordCodificado() {
        User user = new User();
        user.setEmail("nuevo@municipalidad.cl");
        user.setPassword("plain-password");

        Role existingRole = new Role("ROLE_USER");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(existingRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(user, "ROLE_USER");

        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getRoles()).containsExactly(existingRole);
        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_conRolInexistente_deberiaCrearElRolYAsignarloAlUsuario() {
        User user = new User();
        user.setEmail("guest@anonymous.local");
        user.setPassword("random-password");

        when(passwordEncoder.encode("random-password")).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_GUEST")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(user, "ROLE_GUEST");

        assertThat(result.getRoles()).extracting("name").containsExactly("ROLE_GUEST");
        verify(roleRepository, times(1)).save(any(Role.class));
    }
}
