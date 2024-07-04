package dev.oxyac.skinulibot.telegram.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.oxyac.skinulibot.rest.model.Request;
import dev.oxyac.skinulibot.rest.model.Transaction;
import dev.oxyac.skinulibot.rest.model.User;
import dev.oxyac.skinulibot.rest.repository.RequestRepository;
import dev.oxyac.skinulibot.rest.repository.TransactionRepository;
import dev.oxyac.skinulibot.rest.repository.UserRepository;
import dev.oxyac.skinulibot.telegram.TelegramClient;
import dev.oxyac.skinulibot.telegram.callback.CallbackData;
import dev.oxyac.skinulibot.telegram.callback.PayTransactionData;
import dev.oxyac.skinulibot.telegram.callback.StartRequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WebhookService {

    private final RestClient client;
    private final String token;
    private final String webHook;
    private final String secret;
    private final TelegramClient telegramClient;
    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final TransactionRepository transactionRepository;

    public WebhookService(@Value("${bot.token}") String token, @Value("${bot.web-hook}") String webHook, @Value("${bot.secret}") String secret,
                          TelegramClient telegramClient,
                          ObjectMapper objectMapper,
                          UserRepository userRepository,
                          RequestRepository requestRepository,
                          TransactionRepository transactionRepository) {
        this.token = token;
        this.webHook = webHook;
        this.secret = secret;
        this.telegramClient = telegramClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.transactionRepository = transactionRepository;
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

    public void processUpdate(Update update) {
        log.debug("Received update: {}", update);


        if (update.hasInlineQuery()) {

            String query = update.getInlineQuery().getQuery();


            int total = 0;
            try {
                total = Integer.parseInt(query);
            } catch (NumberFormatException e) {
                log.error("Failed to parse query: {}", query);
            }

            Request request = new Request();
            request.setInlineQueryId(update.getInlineQuery().getId());
            request.setAmount(total);
            requestRepository.save(request);
            StartRequestData req = new StartRequestData();
            req.setIq(update.getInlineQuery().getId());
            String json;
            try {
                json = objectMapper.writeValueAsString(req);
            } catch (JsonProcessingException e) {
                log.error(String.valueOf(e));
                return;
            }

            InlineQueryResultArticle allResult = InlineQueryResultArticle
                    .builder()
                    .id(UUID.randomUUID().toString())
                    .title("Подтвердить общую сумму: " + total + " тугриков")
                    .inputMessageContent(InputTextMessageContent
                            .builder()
                            .messageText("Скидываeтся весь чат - " + total + " тугриков")
                            .build())
                    .replyMarkup(InlineKeyboardMarkup
                            .builder()
                            .keyboardRow(
                                    new InlineKeyboardRow(InlineKeyboardButton
                                            .builder()
                                            .text("-> Начать сбор <-")
                                            .callbackData(json)
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
                Boolean a = telegramClient.execute(answerInlineQuery); // Sending our message object to user
            } catch (TelegramApiException e) {
                log.error(String.valueOf(e));
            }
        }
        else if (update.hasCallbackQuery()) {
            log.debug("CallbackQuery: {}", update.getCallbackQuery().getData());
            CallbackData callback;
            try {
                callback = objectMapper.readValue(update.getCallbackQuery().getData(), CallbackData.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to get callbackQuery data", e);
                return;
            }
            if(callback.getA().equals("1")) {
                StartRequestData data;
                try {
                    data = objectMapper.readValue(update.getCallbackQuery().getData(), StartRequestData.class);
                } catch (JsonProcessingException e) {
                    log.error("Failed to get callbackQuery data", e);
                    return;
                }

                User userInitiated = userRepository.findByName(update.getCallbackQuery().getFrom().getUserName());
                if(userInitiated == null) {
                    userInitiated = new User();
                    userInitiated.setName(update.getCallbackQuery().getFrom().getUserName());
                    userRepository.save(userInitiated);
                }
                log.debug(data.toString());
                Request request = requestRepository.findByInlineQueryIdOrderByDateDesc(data.getIq());
                request.setInitiatedBy(userInitiated);
                requestRepository.save(request);

                List<String> members = getChatFullInfo(request.getChatId());

                double perMember = Math.round((request.getAmount() / members.size()) * 100.0) / 100.0;;
                InlineKeyboardRow row = new InlineKeyboardRow();
                members.forEach(member -> {

                    User user = userRepository.findByName(member);
                    if(user == null) {
                        user = new User();
                        user.setName(member);
                        userRepository.save(user);
                    }
                    Transaction transaction = new Transaction();
                    transaction.setCompleted(false);
                    transaction.setAmount(perMember);
                    transaction.setRequest(request);
                    transaction.setUser(user);
                    transactionRepository.save(transaction);


                    PayTransactionData payTransactionData = new PayTransactionData();
                    payTransactionData.setTid(transaction.getId());
                    String json;
                    try {
                        json = objectMapper.writeValueAsString(payTransactionData);
                    } catch (JsonProcessingException e) {
                        log.error(String.valueOf(e));
                        return;
                    }
                    row.add(
                            InlineKeyboardButton.builder()
                                    .text(member + " - " + ((transaction.isCompleted() ? "\\xE2\\x9C\\x94" : "\\xE2\\x9C\\x96")) )
                                    .callbackData(json)
                                    .build());
                });

                InlineKeyboardMarkup markup = InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(
                                row
                        )
                        .build();
                EditMessageReplyMarkup.builder().inlineMessageId(update.getCallbackQuery().getInlineMessageId())
                        .replyMarkup(null)
                        .build();
            }

        } else if (update.hasMessage()) {
            if(update.getMessage().getReplyMarkup() == null) return;

            StartRequestData callbackData = new StartRequestData();
            try{
                callbackData =
                        objectMapper.readValue(update.getMessage().getReplyMarkup().getKeyboard().getFirst().getFirst().getCallbackData(), StartRequestData.class);
            } catch (Exception e) {
                log.error("Failed to parse message", e);
            }

            Chat chat = update.getMessage().getChat();
            log.debug(callbackData.toString());
            Request request = requestRepository.findByInlineQueryIdOrderByDateDesc(callbackData.getIq());
            request.setChatId(chat.getId());
            requestRepository.save(request);
//            long chat_id = update.getMessage().getChatId();
//
//
//            SendMessage message = SendMessage
//                    .builder()
//                    .chatId(chat_id)
//                    .text(message_text)
//                    .replyMarkup(InlineKeyboardMarkup
//                            .builder()
//                            .keyboardRow(
//                                    new InlineKeyboardRow(InlineKeyboardButton
//                                            .builder()
//                                            .text("Update message text")
//                                            .callbackData("update_msg_text")
//                                            .build()
//                                    )
//                            )
//                            .build())
//                    .build();
//            try {
//                telegramClient.execute(message); // Sending our message object to user
//            } catch (TelegramApiException e) {
//                log.error(String.valueOf(e));
//            }
        }

    }

    private List<String> getChatFullInfo(Long chatId) {
        GetChat chatRequest = GetChat.builder().chatId(chatId).build();
        ChatFullInfo chat = new ChatFullInfo();
        log.debug("Chat request: {}", chatRequest.toString());
        try {
            chat = telegramClient.execute(chatRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        log.debug("Chat info: {}", chat.toString());
        return chat.getActiveUsernames();
    }
}