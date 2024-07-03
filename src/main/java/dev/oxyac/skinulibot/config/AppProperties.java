package dev.oxyac.skinulibot.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "bot", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class AppProperties {

    @NotNull
    private String name;
    @NotNull
    private String token;
    @NotNull
    private String webHook;
    @NotNull
    private String secret;

}
