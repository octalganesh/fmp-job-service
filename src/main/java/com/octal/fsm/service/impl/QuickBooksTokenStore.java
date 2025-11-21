package com.octal.fsm.service.impl;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;
import com.octal.fsm.entities.QuickBooksToken;
import com.octal.fsm.repositories.QuickBooksTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuickBooksTokenStore {

    @Autowired
    private QuickBooksTokenRepository tokenRepository;

    @Autowired
    private OAuth2PlatformClientFactory factory;

    // Save token
    public void saveToken(String realmId, String accessToken, String refreshToken, Long expiresInSeconds) {
        QuickBooksToken token = tokenRepository.findByRealmId(realmId).orElse(new QuickBooksToken());
        token.setRealmId(realmId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        tokenRepository.save(token);
    }

    // Get token, refresh if expired
    public QuickBooksToken getToken(String realmId) throws OAuthException {
        QuickBooksToken token = tokenRepository.findByRealmId(realmId).orElseThrow(() -> new RuntimeException("No QuickBooks token found for realmId " + realmId));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // refresh token
            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            BearerTokenResponse response = client.refreshToken(token.getRefreshToken());

            token.setAccessToken(response.getAccessToken());
            token.setRefreshToken(response.getRefreshToken());
            token.setExpiresAt(LocalDateTime.now().plusHours(response.getExpiresIn()));
            token=tokenRepository.save(token);
        }
        return token;
    }
}
