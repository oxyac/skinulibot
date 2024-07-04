package dev.oxyac.skinulibot.telegram.callback;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class StartRequestData extends CallbackData {
    protected String a = "1";
    private String iq;
}
