package dev.oxyac.skinulibot.telegram.callback;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartRequestData extends CallbackData {
    protected String action = "start_request";
    private double total;
    private double perMember;
}
