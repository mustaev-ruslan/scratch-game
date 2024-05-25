package org.scratchgame.domain;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.math.BigDecimal;

@Data
public final class Parameters {
    @Parameter(names = "--config", required = true, description = "config file which is described top of the document")
    private String configPath;
    @Parameter(names = "--betting-amount", required = true, description = "betting amount")
    private BigDecimal bettingAmount;
}
