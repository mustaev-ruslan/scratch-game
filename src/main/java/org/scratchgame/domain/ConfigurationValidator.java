package org.scratchgame.domain;

import org.scratchgame.model.ScratchGameConfiguration;

public class ConfigurationValidator {

    private ConfigurationValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validate(ScratchGameConfiguration configuration) {
        if (configuration.getProbabilities().getStandardSymbols().size() != configuration.getRows() * configuration.getColumns()) {
            throw new IllegalArgumentException("Not all cells have standard probabilities");
        }
    }
}
