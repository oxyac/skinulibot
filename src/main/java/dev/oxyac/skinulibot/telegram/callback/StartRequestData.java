package dev.oxyac.skinulibot.telegram.callback;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartRequestData extends CallbackData {
    protected String a = "1";
    private double total;
    private double perMember;
    private String iQId;
}
