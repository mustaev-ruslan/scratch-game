package org.scratchgame.domain;

import jakarta.annotation.Nullable;
import org.scratchgame.model.*;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.scratchgame.model.Symbol.ImpactEnum;
import static org.scratchgame.model.Symbol.ImpactEnum.MISS;
import static org.scratchgame.model.Symbol.TypeEnum.STANDARD;
import static org.scratchgame.model.WinCombination.WhenEnum.LINEAR_SYMBOLS;
import static org.scratchgame.model.WinCombination.WhenEnum.SAME_SYMBOLS;

public class ScratchGameChecker {

    private final Map<String, StandardSymbol> standardSymbols = new HashMap<>();
    private final Map<String, BonusSymbol> bonusSymbols = new HashMap<>();
    private final Map<String, WinCombination> winCombinations;

    public ScratchGameChecker(ScratchGameConfiguration scratchGameConfiguration) {
        fillSymbols(scratchGameConfiguration.getSymbols());
        winCombinations = scratchGameConfiguration.getWinCombinations();
    }

    private void fillSymbols(Map<String, Symbol> symbols) {
        for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            String name = entry.getKey();
            Symbol symbol = entry.getValue();
            if (symbol.getType() == STANDARD) {
                standardSymbols.put(name, new StandardSymbol(name, symbol.getRewardMultiplier()));
            } else {
                bonusSymbols.put(name, new BonusSymbol(name, symbol.getImpact(), symbol.getRewardMultiplier(), symbol.getExtra()));
            }
        }
    }

    public Output check(Matrix matrix, Input input) {
        List<AppliedWinningCombination> appliedWinningCombinations = findAppliedWinningCombinations(matrix);
        BonusSymbol bonusSymbol = findBonusSymbol(matrix);
        BigDecimal reward = calculateReward(input.getBetAmount(), appliedWinningCombinations, bonusSymbol);
        String appliedBonusSymbol = getAppliedBonusSymbol(appliedWinningCombinations, bonusSymbol);
        return new Output(matrix.toList(), reward, convertToNullableMap(appliedWinningCombinations), appliedBonusSymbol);
    }

    private List<AppliedWinningCombination> findAppliedWinningCombinations(Matrix matrix) {
        Map<String, Map<String, WinCombination>> sameSymbolsWinCombinations = findAppliedSameSymbolsWinningCombinations(matrix);
        Map<String, Map<String, WinCombination>> linearCombinations = findAppliedLinearSymbolsWinningCombinations(matrix);
        Map<String, Map<String, WinCombination>> result = merge(sameSymbolsWinCombinations, linearCombinations);
        return toAppliedWinningCombinations(result);
    }

    private List<AppliedWinningCombination> toAppliedWinningCombinations(Map<String, Map<String, WinCombination>> map) {
        return map.entrySet().stream()
                .map(e -> new AppliedWinningCombination(standardSymbols.get(e.getKey()), e.getValue()))
                .toList();
    }

    private Map<String, Map<String, WinCombination>> merge(Map<String, Map<String, WinCombination>> sameSymbolsWinCombinations, Map<String, Map<String, WinCombination>> linearCombinations) {
        Map<String, Map<String, WinCombination>> result = new HashMap<>(sameSymbolsWinCombinations);
        for (Map.Entry<String, Map<String, WinCombination>> entry : linearCombinations.entrySet()) {
            String symbol = entry.getKey();
            Map<String, WinCombination> linearWinCombinations = entry.getValue();
            Map<String, WinCombination> resultWinCombinations = result.get(symbol);
            if (resultWinCombinations != null) {
                resultWinCombinations.putAll(linearWinCombinations);
            } else {
                result.put(symbol, linearWinCombinations);
            }
        }
        return result;
    }

    private Map<String, Map<String, WinCombination>> findAppliedSameSymbolsWinningCombinations(Matrix matrix) {
        Map<String, Integer> standardSymbolCounts = calculateStandardSymbolCounts(matrix);
        Map<String, Map<String, WinCombination>> result = new HashMap<>();
        for (Map.Entry<String, Integer> countEntry : standardSymbolCounts.entrySet()) {
            String symbolName = countEntry.getKey();
            int count = countEntry.getValue();
            Map<String, String> groups = new HashMap<>();
            for (Map.Entry<String, WinCombination> winCombinationEntry : winCombinations.entrySet()) {
                String winCombinationName = winCombinationEntry.getKey();
                WinCombination winCombination = winCombinationEntry.getValue();
                if (winCombination.getWhen() == SAME_SYMBOLS && count >= winCombination.getCount()) {
                    String existedCombinationName = groups.get(winCombination.getGroup());
                    if (existedCombinationName == null || winCombination.getCount() > winCombinations.get(existedCombinationName).getCount()) {
                        groups.put(winCombination.getGroup(), winCombinationName);
                    }
                }
            }
            if (!groups.isEmpty()) {
                Map<String, WinCombination> combinations = groups.values().stream().collect(toMap(identity(), winCombinations::get));
                result.put(symbolName, combinations);
            }
        }
        return result;
    }

    private Map<String, Map<String, WinCombination>> findAppliedLinearSymbolsWinningCombinations(Matrix matrix) {
        Map<String, Map<String, WinCombination>> result = new HashMap<>();
        for (Map.Entry<String, WinCombination> entry : winCombinations.entrySet()) {
            String winCombinationName = entry.getKey();
            WinCombination winCombination = entry.getValue();
            if (winCombination.getWhen() == LINEAR_SYMBOLS) {
                Set<String> symbols = winCombination.getCoveredAreas().stream()
                        .map(coveredArea -> getSymbolCoveredByArea(matrix, toCellArea(coveredArea)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toSet());
                for (String symbol : symbols) {
                    result.computeIfAbsent(symbol, k -> new HashMap<>()).put(winCombinationName, winCombination);
                }
            }
        }
        return result;
    }

    private List<Cell> toCellArea(List<String> coveredArea) {
        return coveredArea.stream()
                .map(it -> it.split(":"))
                .map(it -> new Cell(parseInt(it[0]), parseInt(it[1])))
                .toList();
    }

    private Optional<String> getSymbolCoveredByArea(Matrix matrix, List<Cell> coveredArea) {
        List<String> values = matrix.getValues(coveredArea);
        String first = values.get(0);
        if (values.stream().allMatch(first::equals)) {
            return Optional.of(first);
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    private Map<String, List<String>> convertToNullableMap(List<AppliedWinningCombination> appliedWinningCombinations) {
        if (appliedWinningCombinations.isEmpty()) {
            return null;
        }
        Map<String, List<String>> result = new HashMap<>();
        for (AppliedWinningCombination combination : appliedWinningCombinations) {
            result.put(combination.standardSymbol().name(), combination.combinations().keySet().stream().sorted().toList());
        }
        return result;
    }

    /**
     * If one symbols matches more than winning combinations then reward should be multiplied.
     * formula: (SYMBOL_1 * WIN_COMBINATION_1_FOR_SYMBOL_1 * WIN_COMBINATION_2_FOR_SYMBOL_1)
     * If the more than one symbols matches any winning combinations then reward should be summed.
     * formula: (SYMBOL_1 * WIN_COMBINATION_1_FOR_SYMBOL_1 * WIN_COMBINATION_2_FOR_SYMBOL_1) + (SYMBOL_2 * WIN_COMBINATION_1_FOR_SYMBOL_2)
     */
    private BigDecimal calculateReward(BigDecimal betAmount, List<AppliedWinningCombination> appliedWinningCombinations, BonusSymbol bonusSymbol) {
        if (appliedWinningCombinations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        for (AppliedWinningCombination combination : appliedWinningCombinations) {
            BigDecimal combinationReward = combination.standardSymbol().rewardMultiplier();
            for (WinCombination winCombination : combination.combinations().values()) {
                combinationReward = combinationReward.multiply(winCombination.getRewardMultiplier());
            }
            result = result.add(combinationReward);
        }
        result = result.multiply(betAmount);
        switch (bonusSymbol.impact()) {
            case MULTIPLY_REWARD -> result = result.multiply(bonusSymbol.rewardMultiplier());
            case EXTRA_BONUS -> result = result.add(bonusSymbol.extra());
        }
        return result;
    }

    @Nullable
    private String getAppliedBonusSymbol(List<AppliedWinningCombination> appliedWinningCombinations, BonusSymbol bonusSymbol) {
        if (appliedWinningCombinations.isEmpty() || bonusSymbol.impact == MISS) {
            return null;
        }
        return bonusSymbol.name;
    }

    private BonusSymbol findBonusSymbol(Matrix matrix) {
        for (String symbolName : matrix) {
            BonusSymbol bonusSymbol = bonusSymbols.get(symbolName);
            if (bonusSymbol != null) {
                return bonusSymbol;
            }
        }
        return new BonusSymbol("MISS", MISS, null, null);
    }

    private Map<String, Integer> calculateStandardSymbolCounts(Matrix matrix) {
        Map<String, Integer> standardSymbolCounts = new HashMap<>();
        for (String symbolName : matrix) {
            if (standardSymbols.containsKey(symbolName)) {
                standardSymbolCounts.merge(symbolName, 1, Integer::sum);
            }
        }
        return standardSymbolCounts;
    }

    private record BonusSymbol(
            String name,
            ImpactEnum impact,
            @Nullable BigDecimal rewardMultiplier,
            @Nullable BigDecimal extra
    ) {
    }

    private record StandardSymbol(String name, BigDecimal rewardMultiplier) {
    }

    private record AppliedWinningCombination(StandardSymbol standardSymbol, Map<String, WinCombination> combinations) {
    }
}
