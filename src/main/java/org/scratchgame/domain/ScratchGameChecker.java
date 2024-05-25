package org.scratchgame.domain;

import org.scratchgame.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScratchGameChecker {

    private final int columns;
    private final int rows;
    private final Map<String, Symbol> symbols;
    private final Map<String, WinCombination> winCombinations;

    public ScratchGameChecker(ScratchGameConfiguration scratchGameConfiguration) {
        columns = scratchGameConfiguration.getColumns();
        rows = scratchGameConfiguration.getRows();
        symbols = scratchGameConfiguration.getSymbols();
        winCombinations = scratchGameConfiguration.getWinCombinations();
    }

    public Output check(Matrix matrix, Input input) {
        BigDecimal reward = BigDecimal.ZERO;
        Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
        String appliedBonusSymbol = "";
        // TODO
        return new Output(matrix.toList(), reward, appliedWinningCombinations, appliedBonusSymbol);
    }
}
