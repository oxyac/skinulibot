package dev.oxyac.skinulibot.telegram.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.oxyac.skinulibot.service.WebhookService;
import dev.oxyac.skinulibot.telegram.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultsButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.ArrayList;
import java.util.UUID;

@Component
@Slf4j
public class WebhookBot extends SpringTelegramWebhookBot {

    private final WebhookService webhookService;
    private final TelegramClient telegramClient;
    private final ObjectMapper objectMapper;

    public WebhookBot(WebhookService webhookService, TelegramClient telegramClient, ObjectMapper objectMapper) {
        super(
                "update",
                update -> {
                    log.debug("Received update: {}", update);


                    if (update.hasInlineQuery()) {

                        InlineQueryResultArticle allResult = InlineQueryResultArticle
                                .builder()
                                .id(UUID.randomUUID().toString())
                                .title("Скидывается весь чат")
                                .inputMessageContent(InputTextMessageContent
                                        .builder()
                                        .messageText("Скидывается весь чат 3")
                                        .build())
                                .replyMarkup(InlineKeyboardMarkup
                                        .builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(InlineKeyboardButton
                                                        .builder()
                                                        .text("Скидывается весь чат 4")
                                                        .callbackData("update_msg_text")
                                                        .build()
                                                )
                                        )
                                        .build())
                                .build();
                        AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder()
                            .inlineQueryId(update.getInlineQuery().getId())
                            .results(new ArrayList<>() {{
                                add(allResult);
                            }})
                            .build();

                        try {
                            log.debug("Sending answerInlineQuery: {}", answerInlineQuery);
                            String body = objectMapper.writeValueAsString(answerInlineQuery);
                            log.debug("Sending answerInlineQuery JSON: {}", body);
                            telegramClient.execute(answerInlineQuery); // Sending our message object to user
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
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
        this.objectMapper = objectMapper;
    }
}
