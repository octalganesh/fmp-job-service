package com.octal.fsm.controller;

import javax.servlet.http.HttpSession;

import com.intuit.oauth2.exception.ConnectionException;
import com.octal.fsm.configuration.OAuth2PlatformClientFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.PlatformResponse;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;


@RestController
public class RevokeTokenController {

    @Autowired
    OAuth2PlatformClientFactory factory;


    @ResponseBody
    @GetMapping("/revokeToken")
    public String revokeToken(HttpSession session) {

        String failureMsg = "Failed";

        try {

            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            String refreshToken = (String) session.getAttribute("refresh_token");
            PlatformResponse response = client.revokeToken(refreshToken);
            return new JSONObject().put("response", "Revoke successful").toString();
        } catch (ConnectionException ex) {
            return new JSONObject().put("response", ex.getResponseContent()).toString();
        } catch (Exception ex) {
            return new JSONObject().put("response", failureMsg).toString();
        }

    }

}