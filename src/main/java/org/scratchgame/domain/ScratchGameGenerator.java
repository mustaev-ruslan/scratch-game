package org.scratchgame.domain;

import org.scratchgame.model.Probabilities;
import org.scratchgame.model.ScratchGameConfiguration;
import org.scratchgame.model.StandardSymbol;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.random.RandomGenerator;

public class ScratchGameGenerator {

    private final int columns;
    private final int rows;
    private final Probabilities probabilities;
    private final RandomGenerator random;

    public ScratchGameGenerator(ScratchGameConfiguration scratchGameConfiguration) {
        columns = scratchGameConfiguration.getColumns();
        rows = scratchGameConfiguration.getRows();
        probabilities = scratchGameConfiguration.getProbabilities();
        random = new Random();
    }

    public Matrix generate() {
        String[][] result = generateStandardSymbols();
        BonusSymbol bonusSymbol = generateBonusSymbol();
        result[bonusSymbol.row()][bonusSymbol.column()] = bonusSymbol.value();
        return new Matrix(result);
    }

    private BonusSymbol generateBonusSymbol() {
        int row = random.nextInt(rows);
        int column = random.nextInt(columns);
        String value = generateRandomSymbol(probabilities.getBonusSymbols().getSymbols());
        return new BonusSymbol(row, column, value);
    }

    private String generateRandomSymbol(Map<String, BigDecimal> symbols) {
        NavigableMap<Double, String> accumulatedProbabilities = new TreeMap<>();
        double accumulatedProbability = 0;
        for (Map.Entry<String, BigDecimal> entry : symbols.entrySet()) {
            accumulatedProbability += entry.getValue().doubleValue();
            accumulatedProbabilities.put(accumulatedProbability, entry.getKey());
        }
        double randomValue = random.nextDouble() * accumulatedProbability;
        return accumulatedProbabilities.higherEntry(randomValue).getValue();
    }

    private String[][] generateStandardSymbols() {
        String[][] result = new String[rows][columns];
        for (StandardSymbol standardSymbol : probabilities.getStandardSymbols()) {
            result[standardSymbol.getRow()][standardSymbol.getColumn()] = generateRandomSymbol(standardSymbol.getSymbols());
        }
        return result;
    }

    private record BonusSymbol(int row, int column, String value) {
    }

}
