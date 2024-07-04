package dev.oxyac.skinulibot.telegram.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.oxyac.skinulibot.rest.repository.RequestRepository;
import dev.oxyac.skinulibot.rest.repository.TransactionRepository;
import dev.oxyac.skinulibot.rest.repository.UserRepository;
import dev.oxyac.skinulibot.telegram.service.WebhookService;
import dev.oxyac.skinulibot.telegram.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

@Component
@Slf4j
public class WebhookBot extends SpringTelegramWebhookBot {

    private final WebhookService webhookService;
    private final TelegramClient telegramClient;
    private final ObjectMapper objectMapper;

    public WebhookBot(WebhookService webhookService,
                      TelegramClient telegramClient,
                      ObjectMapper objectMapper,
                      UserRepository userRepository,
                      RequestRepository requestRepository,
                      TransactionRepository transactionRepository) {
        super(
                "update",
                update -> {
                    webhookService.processUpdate(update);
                    return null;
                },
                webhookService::assignToken,
                webhookService::removeToken
        );
        this.webhookService = webhookService;

        this.telegramClient = telegramClient;
        this.objectMapper = objectMapper;
    }
}
