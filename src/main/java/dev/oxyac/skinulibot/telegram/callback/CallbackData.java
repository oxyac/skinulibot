package dev.oxyac.skinulibot.telegram.callback;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class CallbackData {
    protected String action = null;
}
