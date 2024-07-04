package dev.oxyac.skinulibot.telegram.bot;

import dev.oxyac.skinulibot.service.WebhookService;
import dev.oxyac.skinulibot.telegram.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.function.Function;

@Component
@Slf4j
public class WebhookBot extends SpringTelegramWebhookBot {

    private final WebhookService webhookService;
    private final TelegramClient telegramClient;
    public WebhookBot(WebhookService webhookService, TelegramClient telegramClient) {
        super(
                "update",
                update -> {
                    String message_text = update.getMessage().getText();
                    long chat_id = update.getMessage().getChatId();

                    log.debug("Received message: {}", message_text);

                    SendMessage message = SendMessage // Create a message object
                            .builder()
                            .chatId(chat_id)
                            .text(message_text)
                            .build();
                    log.debug("Chat id: {}", message.toString());

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
        this.webhookService = webhookService;

        this.telegramClient = telegramClient;
    }
}