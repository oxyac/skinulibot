package dev.oxyac.skinulibot.service;

import dev.oxyac.skinulibot.telegram.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class WebhookService {

    private final RestClient client;
    private final String token;
    private final String webHook;
    private final String secret;
    private final TelegramClient telegramClient;

    public WebhookService(@Value("${bot.token}") String token, @Value("${bot.web-hook}") String webHook, @Value("${bot.secret}") String secret, TelegramClient telegramClient) {
        this.token = token;
        this.webHook = webHook;
        this.secret = secret;
        this.telegramClient = telegramClient;
        this.client = RestClient.create("https://api.telegram.org");
    }

    public void assignToken() {

        try {
            WebhookInfo webhookInfo = telegramClient.execute(new GetWebhookInfo());
            log.debug("Webhook info: {}", webhookInfo.toString());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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