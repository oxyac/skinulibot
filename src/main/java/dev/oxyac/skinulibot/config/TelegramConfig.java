package dev.oxyac.skinulibot.config;

import dev.oxyac.skinulibot.service.WebhookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;
@Configuration
public class TelegramConfig {
    public final TelegramBotsSpringWebhookApplication telegramApplication;
    private final WebhookService webhookService;

    public TelegramConfig(TelegramBotsSpringWebhookApplication telegramApplication, WebhookService webhookService) {
        this.telegramApplication = telegramApplication;
        this.webhookService = webhookService;

        webhookBot();
    }
    public void webhookBot() {
        try {
            telegramApplication.registerBot(
                    "update",
                    update -> {
                        return new GetMe();
                    },
                    webhookService::assignToken,
                    webhookService::removeToken
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
