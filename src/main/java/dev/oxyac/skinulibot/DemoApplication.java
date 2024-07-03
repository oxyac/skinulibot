package dev.oxyac.skinulibot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DemoApplication {
    public final TelegramBotsSpringWebhookApplication telegramApplication;

    public DemoApplication(TelegramBotsSpringWebhookApplication telegramApplication) {
        this.telegramApplication = telegramApplication;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}
