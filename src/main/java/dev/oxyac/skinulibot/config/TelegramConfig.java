package dev.oxyac.skinulibot.config;

import dev.oxyac.skinulibot.service.WebhookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;
@Configuration
public class TelegramConfig {
    public final TelegramBotsSpringWebhookApplication telegramApplication;
    private final WebhookService webhookService;

    private final TelegramClient telegramClient;

    public TelegramConfig(TelegramBotsSpringWebhookApplication telegramApplication, WebhookService webhookService, TelegramClient telegramClient) {
        this.telegramApplication = telegramApplication;
        this.webhookService = webhookService;
        this.telegramClient = telegramClient;

        webhookBot();
    }
    public void webhookBot() {
        try {
            telegramApplication.registerBot(
                    "update",
                    update -> {
                        String message_text = update.getMessage().getText();
                        long chat_id = update.getMessage().getChatId();

                        SendMessage message = SendMessage // Create a message object
                                .builder()
                                .chatId(chat_id)
                                .text(message_text)
                                .build();
                        try {
                            telegramClient.execute(message); // Sending our message object to user
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        return null;
                    },
                    webhookService::assignToken,
                    webhookService::removeToken
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
