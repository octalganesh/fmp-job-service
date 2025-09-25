package com.octal.fsm.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class PublicIpInitializer {

    @PostConstruct
    public void setPublicIpForEureka() {
        try {
            String publicIp = getPublicIpFromExternalService();

            if (publicIp != null && !publicIp.isEmpty()) {
                System.setProperty("EUREKA_INSTANCE_IP", publicIp);
                System.out.println("✅ EUREKA_INSTANCE_IP set to: " + publicIp);
            } else {
                System.out.println("⚠️ Failed to fetch public IP for Eureka registration.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error setting public IP for Eureka:");
            e.printStackTrace();
        }
    }

    private String getPublicIpFromExternalService() {
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next().trim() : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
