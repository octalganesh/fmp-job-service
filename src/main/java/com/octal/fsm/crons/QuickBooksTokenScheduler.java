package com.octal.fsm.crons;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;
import com.octal.fsm.entities.QuickBooksToken;
import com.octal.fsm.repositories.QuickBooksTokenRepository;
import com.octal.fsm.service.impl.QuickBooksTokenStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QuickBooksTokenScheduler {
    private static final Logger LOGGER = LogManager.getLogger(QuickBooksTokenScheduler.class);
    @Value("${quickbooks.company-id}")
    private String realmId;

    @Autowired
    private QuickBooksTokenRepository tokenRepository;

    @Autowired
    private OAuth2PlatformClientFactory factory;

    @Autowired
    private QuickBooksTokenStore quickBooksTokenStore;

    @Scheduled(cron = "0 */5 * * * *")
    public void refreshTokens() throws OAuthException {
        LOGGER.info("refresh token method called - " + LocalDateTime.now());
        try {
            QuickBooksToken token = tokenRepository.findByRealmId(realmId).orElseThrow(() -> new RuntimeException("No QuickBooks token found for realmId " + realmId));

            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            BearerTokenResponse response = client.refreshToken(token.getRefreshToken());
            token.setUpdatedAt(LocalDateTime.now());
            token.setAccessToken(response.getAccessToken());
            token.setRefreshToken(response.getRefreshToken());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(response.getExpiresIn()));
            token = tokenRepository.save(token);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
