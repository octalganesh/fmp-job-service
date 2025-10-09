package com.octal.fsm.controller;

import com.intuit.ipp.exception.InvalidRequestException;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Scope;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;
import com.octal.fsm.service.impl.QuickBooksTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/quickbooks/oauth2")
public class QuickBooksOAuthController {

    @Autowired
    private OAuth2PlatformClientFactory clientFactory;

    @Autowired
    private QuickBooksTokenService tokenService;

    @Value("${quickbooks.company-id}")
    private String companyId;
    @Value("${quickbooks.oauth2.redirect-uri}")
    private String redirectUri;

    @GetMapping("/connect")
    public RedirectView connectToQuickBooks() throws InvalidRequestException, com.intuit.oauth2.exception.InvalidRequestException {
        // Get OAuth2 config and client
        OAuth2PlatformClient client = clientFactory.getOAuth2PlatformClient();

        // Generate CSRF token (optional but recommended)
        String csrf = clientFactory.getOAuth2Config().generateCSRFToken();

        // Set scopes
        List<Scope> scopes = new ArrayList<>();
        scopes.add(Scope.Accounting);
        scopes.add(Scope.Payments); // optional if needed

        // Prepare the authorization URL
        String authUrl = clientFactory.getOAuth2Config().prepareUrl(scopes, redirectUri, csrf);

        // Redirect the user to QuickBooks OAuth page
        return new RedirectView(authUrl, true, true, false);
    }

    @GetMapping("/callback")
    public String oauth2Callback(@RequestParam String code, @RequestParam String state) throws Exception {

        OAuth2PlatformClient client = clientFactory.getOAuth2PlatformClient();

        // Exchange authorization code for tokens
        var tokenResponse = client.retrieveBearerTokens(code, companyId);

        // Save tokens for later use
        tokenService.setTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), tokenResponse.getExpiresIn());

        return "QuickBooks Authorization Successful!";
    }
}
