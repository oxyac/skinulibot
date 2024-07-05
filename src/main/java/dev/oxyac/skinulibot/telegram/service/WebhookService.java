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
import java.util.Collection;
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

            String[] strArray = query.split(" ");

            double perMember = 0;
            double total = 0;
            Request request = new Request();
            request.setInlineQueryId(update.getInlineQuery().getId());

            User userInitiated = userRepository.findByName(update.getInlineQuery().getFrom().getUserName());
            if(userInitiated == null) {
                userInitiated = new User();
                userInitiated.setName(update.getInlineQuery().getFrom().getUserName());
                userRepository.save(userInitiated);
            }
            for (int i = 0; i < strArray.length; i++) {
                if(i == 0){
                    if(!strArray[i].matches("^[0-9]+(\\.[0-9]+)?$")) {
                        return;
                    }
                    total = Double.parseDouble(strArray[i]);
                    request.setAmount(total);
                    request.setInitiatedBy(userInitiated);
                    requestRepository.save(request);

                    perMember = Math.round((total / (strArray.length - 1)) * 100.0) / 100.0;
                    continue;
                }

                User user = userRepository.findByName(strArray[i]);
                if(user == null) {
                    user = new User();
                    user.setName(strArray[i]);
                    userRepository.save(user);
                }
                Transaction transaction = new Transaction();
                transaction.setCompleted(false);
                transaction.setRequest(request);
                transaction.setUser(user);
                transaction.setAmount(perMember);
                transactionRepository.save(transaction);


            }

            InlineKeyboardMarkup markup = this.buildReplyMarkup(request);
            if(markup == null) return;

            InlineQueryResultArticle allResult = InlineQueryResultArticle
                    .builder()
                    .id(UUID.randomUUID().toString())
                    .title("Подтвердить пользователей")
                    .inputMessageContent(InputTextMessageContent
                            .builder()
                            .messageText("Скидываются " + (strArray.length - 1) +  " пользователей на общую сумму в - " + total + " тугриков. \n" +
                                    "С каждого по " + perMember + " тугриков. \n" +
                                    "Скидывать сюда - " + userInitiated.getName() + " ")
                            .build())
                    .replyMarkup(markup)
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
            if(callback.getA().equals("pay")) {
                PayTransactionData payTransactionData;
                try {
                    payTransactionData = objectMapper.readValue(update.getCallbackQuery().getData(), PayTransactionData.class);
                } catch (JsonProcessingException e) {
                    log.error("Failed to get callbackQuery data", e);
                    return;
                }
                Transaction transaction = transactionRepository.findById(payTransactionData.getTid()).orElse(null);
                if(transaction == null) {
                    log.error("Transaction not found");
                    return;
                }
                transaction.setCompleted(!transaction.isCompleted());
                transactionRepository.save(transaction);

                InlineKeyboardMarkup markup = this.buildReplyMarkup(transaction.getRequest());


                EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                        .inlineMessageId(transaction.getRequest().getInlineQueryId())
                        .replyMarkup(markup)
                        .build();
                try {
                    telegramClient.execute(editMessageReplyMarkup); // Sending our message object to user
                } catch (TelegramApiException e) {
                    log.error(String.valueOf(e));
                }

            }

        } else if (update.hasMessage()) {
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

    private InlineKeyboardMarkup buildReplyMarkup(Request request) {
        ArrayList<Transaction> transactions = transactionRepository.findByRequest(request);
        if(transactions == null) {
            return null;
        }
        InlineKeyboardRow row = new InlineKeyboardRow();

        Collection<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (Transaction transaction : transactions) {
            PayTransactionData payTransactionData = new PayTransactionData();
            payTransactionData.setTid(transaction.getId());
            String json;
            try {
                json = objectMapper.writeValueAsString(payTransactionData);
            } catch (JsonProcessingException e) {
                log.error(String.valueOf(e));
                return null;
            }
            row.add(
                    InlineKeyboardButton.builder()
                            .text(transaction.getUser().getName() + " - " + ((transaction.isCompleted() ? "✔" : "✖")) )
                            .callbackData(json)
                            .build());

            if(row.size() == 2) {
                keyboard.add(row);
            }
        }


        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}