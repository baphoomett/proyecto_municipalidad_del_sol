package muni_del_valle.ms_usuarios.ms_usuarios.controller;

import jakarta.validation.Valid;
import muni_del_valle.ms_usuarios.ms_usuarios.dto.AuthRequest;
import muni_del_valle.ms_usuarios.ms_usuarios.dto.AuthResponse;
import muni_del_valle.ms_usuarios.ms_usuarios.dto.RegisterRequest;
import muni_del_valle.ms_usuarios.ms_usuarios.model.User;
import muni_del_valle.ms_usuarios.ms_usuarios.service.UserService;
import muni_del_valle.ms_usuarios.ms_usuarios.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(req.getPassword());
        u.setFullName(req.getFullName());
        User created = userService.createUser(u, "ROLE_USER");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            var userDetails = userService.loadUserByUsername(req.getEmail());
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_USER");
            String token = jwtUtil.generateToken(req.getEmail(), role);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<?> guest() {
        // create a transient guest user and return token so clients can report without registering
        String guestEmail = "guest+" + java.util.UUID.randomUUID() + "@anonymous.local";
        User u = new User();
        u.setEmail(guestEmail);
        // set a random password that nobody knows
        u.setPassword(java.util.UUID.randomUUID().toString());
        User created = userService.createUser(u, "ROLE_GUEST");
        String token = jwtUtil.generateToken(created.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
