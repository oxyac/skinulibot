package dev.oxyac.skinulibot.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

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
