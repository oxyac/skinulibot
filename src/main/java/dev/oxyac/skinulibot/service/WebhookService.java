package dev.oxyac.skinulibot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class WebhookService {

    private final RestClient client;
    private final String token;
    private final String webHook;
    private final String secret;

    public WebhookService(@Value("${bot.token}") String token, @Value("${bot.web-hook}") String webHook, @Value("${bot.secret}") String secret) {
        this.token = token;
        this.webHook = webHook;
        this.secret = secret;
        this.client = RestClient.create("https://api.telegram.org");
    }

    public void assignToken() {
        client.post()
                .uri("/bot%s/setWebhook?url=%s&secret_token=%s".formatted(token, webHook, secret))
                .retrieve()
                .toBodilessEntity();
    }

    public void removeToken() {
        client.post()
                .uri("/bot%s/deleteWebhook?url=%s".formatted(token, webHook))
                .retrieve()
                .toBodilessEntity();
    }
}