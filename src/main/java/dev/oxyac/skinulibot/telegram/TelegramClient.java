package dev.oxyac.skinulibot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

@Component
public class TelegramClient extends OkHttpTelegramClient {
    public String token;
    public TelegramClient(@Value("${bot.token}") String token) {
        super("bow%s".formatted(token));
    }
}
