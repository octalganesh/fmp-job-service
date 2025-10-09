package com.octal.fsm.configuration;

import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuickBooksConnectionConfig {

    @Value("${quickbooks.oauth2.client-id}")
    private String clientId;

    @Value("${quickbooks.oauth2.client-secret}")
    private String clientSecret;

    @Value("${quickbooks.oauth2.redirect-uri}")
    private String redirectUri;

    public View connectToQuickBooks() throws InvalidRequestException {

        // Initialize the config
        com.intuit.oauth2.config.OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(clientId, clientSecret).callDiscoveryAPI(Environment.SANDBOX).buildConfig();

        // Generate CSRF token
        String csrf = oauth2Config.generateCSRFToken();

        // Prepare scopes
        List<Scope> scopes = new ArrayList<>();
        scopes.add(Scope.Accounting);
        scopes.add(Scope.Payments);


        // Prepare authorization URL to initiate the OAuth handshake
        return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, csrf), true, true, false);
    }


}
