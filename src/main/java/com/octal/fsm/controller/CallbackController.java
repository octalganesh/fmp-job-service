package com.octal.fsm.controller;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;
import com.octal.fsm.service.impl.QuickBooksTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class CallbackController {

    @Autowired
    OAuth2PlatformClientFactory factory;

    @Value("${quickbooks.oauth2.redirect-uri}")
    private String redirectUri;
    @Autowired
    private QuickBooksTokenStore tokenStore;

    @GetMapping("/oauth2redirect")
    public String callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state, @RequestParam(value = "realmId", required = false) String realmId, HttpSession session) {
        try {
            String csrfToken = (String) session.getAttribute("csrfToken");
//	        if (csrfToken.equals(state)) {
            session.setAttribute("realmId", realmId);
            session.setAttribute("auth_code", authCode);

            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            String redirectUri = factory.getPropertyValue("quickbooks.oauth2.redirect-uri");

            BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

            session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
            session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());
            // Save tokens in memory using realmId as key
//             tokenStore.saveToken(
//                     realmId,
//                     bearerTokenResponse.getAccessToken(),
//                     bearerTokenResponse.getRefreshToken()
//             );
            tokenStore.saveToken(realmId, bearerTokenResponse.getAccessToken(), bearerTokenResponse.getRefreshToken(), bearerTokenResponse.getExpiresIn());
            // Update your Data store here with user's AccessToken and RefreshToken along with the realmId

            return "connected";
//	        }
        } catch (OAuthException e) {
        }
        return null;
    }


}