package dev.oxyac.skinulibot.telegram.callback;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PayTransactionData extends CallbackData {
    protected String a = "pay";
    private Long tid;
}
