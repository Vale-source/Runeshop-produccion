package com.example.runeshop_ecommerce.auth;

import com.example.runeshop_ecommerce.config.JwtService;
import com.example.runeshop_ecommerce.entities.RefreshToken;
import com.example.runeshop_ecommerce.entities.Usuario;
import com.example.runeshop_ecommerce.entities.enums.Role;
import com.example.runeshop_ecommerce.exception.NotFoundException;
import com.example.runeshop_ecommerce.exception.NotProvideRefreshTokenException;
import com.example.runeshop_ecommerce.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getNombreUsuario(), request.getContrasenia()));

        Usuario usuario = usuarioRepository
                .findUsuarioByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        refreshTokenService.deleteByUsuario(usuario);

        String accessToken = jwtService.getToken(usuario);
        String refreshToken = refreshTokenService.createRefreshToken(usuario.getNombreUsuario()).getToken();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .id(usuario.getId())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findUsuarioByNombreUsuario(request.getNombreUsuario()).isPresent()) {
            throw new IllegalArgumentException("Nombre de usuario ya registrado");
        }
        if (usuarioRepository.findUsuarioByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        if (usuarioRepository.findUsuarioByDni(request.getDni()).isPresent()) {
            throw new IllegalArgumentException("DNI ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .contrasenia(passwordEncoder.encode(request.getContrasenia()))
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .dni(request.getDni())
                .tipoUsuario(Role.USER)
                .email(request.getEmail())
                .build();

        usuarioRepository.save(usuario);

        String accessToken = jwtService.getToken(usuario);
        String refreshToken = refreshTokenService.createRefreshToken(usuario.getNombreUsuario()).getToken();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshToken(String refreshTokenRequest) {

        if (refreshTokenRequest == null || refreshTokenRequest.isBlank()) {
            throw new NotProvideRefreshTokenException("Refresh token no proporcionado");
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new NotFoundException("Refresh Token no encontrado"));

        refreshTokenService.verifyRefreshTokenExpiration(refreshToken);

        Usuario usuario = refreshToken.getUsuario();

        String newAccesToken = jwtService.getToken(usuario);

        refreshTokenService.deleteByToken(refreshTokenRequest);

        String newRefreshToken = refreshTokenService.createRefreshToken(usuario.getNombreUsuario()).getToken();

        return AuthResponse.builder()
                .token(newAccesToken)
                .refreshToken(newRefreshToken)
                .id(usuario.getId())
                .build();
    }
}
