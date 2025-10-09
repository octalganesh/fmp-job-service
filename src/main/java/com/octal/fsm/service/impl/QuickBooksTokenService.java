package com.octal.fsm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;

@Service
public class QuickBooksTokenService {

    @Autowired
    private OAuth2PlatformClientFactory clientFactory;

    private String accessToken;
    private String refreshToken;
    private long tokenExpiryTime;

    /**
     * Returns a valid access token, refreshes if expired
     */
    public synchronized String getAccessToken() throws Exception {
        long now = System.currentTimeMillis();

        // If token is missing or expired, refresh it
        if (accessToken == null || now >= tokenExpiryTime) {
            refreshAccessToken();
        }

        return accessToken;
    }

    /**
     * Refreshes the access token using the refresh token
     */
    private void refreshAccessToken() throws Exception {
        OAuth2PlatformClient client = clientFactory.getOAuth2PlatformClient();

        if (refreshToken == null) {
            throw new Exception("No refresh token available. Please authorize the app first.");
        }

        var response = client.refreshToken(refreshToken);

        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
        this.tokenExpiryTime = System.currentTimeMillis() + (response.getExpiresIn() - 60) * 1000; // buffer 60s
    }

    /**
     * Call this once after the OAuth2 handshake to store tokens
     */
    public void setTokens(String accessToken, String refreshToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiryTime = System.currentTimeMillis() + (expiresInSeconds - 60) * 1000;
    }
}
