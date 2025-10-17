package com.capstone.capstone.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleConfig {
    @Value("${google.clientId}")
    private String clientId;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory factor = GsonFactory.getDefaultInstance();
        return new GoogleIdTokenVerifier.Builder(transport, factor)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
}
