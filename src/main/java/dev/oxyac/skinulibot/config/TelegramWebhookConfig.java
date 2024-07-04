package dev.oxyac.skinulibot.config;

import dev.oxyac.skinulibot.telegram.bot.WebhookBot;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;

@Configuration
public class TelegramWebhookConfig {
    public final TelegramBotsSpringWebhookApplication telegramApplication;
    public final WebhookBot webhookBot;

    public TelegramWebhookConfig(TelegramBotsSpringWebhookApplication telegramApplication, WebhookBot webhookBot) {
        this.telegramApplication = telegramApplication;
        this.webhookBot = webhookBot;

        try {
            telegramApplication.registerBot(webhookBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
