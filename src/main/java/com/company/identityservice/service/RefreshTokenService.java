package com.company.identityservice.service;

import com.company.identityservice.model.RefreshToken;
import com.company.identityservice.repository.RefreshTokenRepository;
import com.company.identityservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        // જો જૂનો ટોકન હોય તો પહેલાં સેફ્ટી માટે કાઢી નાખીએ
        try {
            refreshTokenRepository.deleteByUser_Username(username);
        } catch (Exception e) { }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findByUsername(username).orElseThrow());
        refreshToken.setToken(UUID.randomUUID().toString()); // મસ્ત યુનિક આઈડી
        refreshToken.setExpiryDate(Instant.now().plusMillis(600000)); // ૧૦ મિનિટ માટે વેલિડ (તમે વધારી શકો)

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public java.util.Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}