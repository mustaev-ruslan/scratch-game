package org.scratchgame.domain;

import org.scratchgame.model.Input;

import java.math.BigDecimal;

public class InputValidator {

    private InputValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validate(Input input) {
        BigDecimal betAmount = input.getBetAmount();
        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Betting amount should be bigger than zero, but was " + betAmount);
        }
    }
}
