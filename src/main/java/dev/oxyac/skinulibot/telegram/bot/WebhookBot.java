package dev.oxyac.skinulibot.telegram.bot;

import dev.oxyac.skinulibot.service.WebhookService;
import dev.oxyac.skinulibot.telegram.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultsButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
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
                    log.debug("Received update: {}", update);


                    if (update.hasInlineQuery()) {
                        AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder()
                                .inlineQueryId(update.getInlineQuery().getId())
                                .button(InlineQueryResultsButton
                                        .builder()
                                        .text("All members")
                                        .startParameter("")
                                        .build())
                                .build();
                        try {
                            telegramClient.execute(answerInlineQuery); // Sending our message object to user
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (update.hasMessage()) {
                        String message_text = update.getMessage().getText();
                        long chat_id = update.getMessage().getChatId();


                        SendMessage message = SendMessage // Create a message object
                                .builder()
                                .chatId(chat_id)
                                .text(message_text)
                                .replyMarkup(InlineKeyboardMarkup
                                        .builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(InlineKeyboardButton
                                                        .builder()
                                                        .text("Update message text")
                                                        .callbackData("update_msg_text")
                                                        .build()
                                                )
                                        )
                                        .build())
                                .build();
                        try {
                            telegramClient.execute(message); // Sending our message object to user
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
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
