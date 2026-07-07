package com.santiago.base.modules.auth.service;

import com.santiago.base.core.exceptions.BusinessException;
import com.santiago.base.core.security.JwtService;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.auth.dto.AuthResponseDTO;
import com.santiago.base.modules.auth.dto.LoginRequestDTO;
import com.santiago.base.modules.auth.dto.RegisterRequestDTO;
import com.santiago.base.modules.auth.refresh.service.InvalidRefreshTokenException;
import com.santiago.base.modules.auth.refresh.service.RefreshTokenService;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("user.email.alreadyExists", request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
        String accessToken = jwtService.generateAccessToken(new UserSessionModel(rotation.user()));
        String newRefreshToken = rotation.newRawToken();
        User user = rotation.user();

        return new AuthResponseDTO(
                accessToken,
                newRefreshToken,
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        try {
            refreshTokenService.revoke(rawRefreshToken);
        } catch (InvalidRefreshTokenException ignored) {
        }
    }

    @Transactional
    public void logoutAll(UserSessionModel requestUser) {
        refreshTokenService.revokeAllForUser(requestUser.getId());
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(new UserSessionModel(user));
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponseDTO(accessToken, refreshToken, user.getEmail(), user.getName(), user.getRole());
    }
}