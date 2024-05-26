package org.scratchgame.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.scratchgame.model.Input;
import org.scratchgame.model.Output;
import org.scratchgame.model.ScratchGameConfiguration;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.BigDecimalComparator.BIG_DECIMAL_COMPARATOR;

class ScratchGameCheckerTest {


    public static Object[][] checkData() {
        return new Object[][]{
                {"3x3_1", 100, new String[][]{
                        {"A", "A", "A"},
                        {"A", "A", "A"},
                        {"A", "A", "A"},
                }, new Output(List.of(
                        List.of("A", "A", "A"),
                        List.of("A", "A", "A"),
                        List.of("A", "A", "A")),
                        BigDecimal.valueOf(10_000_000), Map.of(
                        "A", List.of(
                                "same_symbol_9_times",
                                "same_symbols_diagonally_left_to_right",
                                "same_symbols_diagonally_right_to_left",
                                "same_symbols_horizontally",
                                "same_symbols_vertically")),
                        null)},

                {"3x3_1", 100, new String[][]{
                        {"A", "A", "A"},
                        {"A", "A", "A"},
                        {"A", "A", "MISS"},
                }, new Output(List.of(
                        List.of("A", "A", "A"),
                        List.of("A", "A", "A"),
                        List.of("A", "A", "MISS")),
                        BigDecimal.valueOf(1_000_000), Map.of(
                        "A", List.of(
                                "same_symbol_8_times",
                                "same_symbols_diagonally_right_to_left",
                                "same_symbols_horizontally",
                                "same_symbols_vertically")),
                        null)},

                {"3x3_1", 100, new String[][]{
                        {"A", "A", "A"},
                        {"A", "10x", "A"},
                        {"A", "A", "A"},
                }, new Output(List.of(
                        List.of("A", "A", "A"),
                        List.of("A", "10x", "A"),
                        List.of("A", "A", "A")),
                        BigDecimal.valueOf(2_000_000), Map.of(
                        "A", List.of(
                                "same_symbol_8_times",
                                "same_symbols_horizontally",
                                "same_symbols_vertically")),
                        "10x")},

                {"3x3_1", 100, new String[][]{
                        {"A", "A", "A"},
                        {"A", "A", "A"},
                        {"A", "A", "XXX"},
                }, new Output(List.of(
                        List.of("A", "A", "A"),
                        List.of("A", "A", "A"),
                        List.of("A", "A", "XXX")),
                        BigDecimal.valueOf(1_000_000), Map.of(
                        "A", List.of(
                                "same_symbol_8_times",
                                "same_symbols_diagonally_right_to_left",
                                "same_symbols_horizontally",
                                "same_symbols_vertically")),
                        null)},

                {"3x3_1", 100, new String[][]{
                        {"A", "A", "B"},
                        {"A", "+1000", "B"},
                        {"A", "A", "B"},
                }, new Output(List.of(
                        List.of("A", "A", "B"),
                        List.of("A", "+1000", "B"),
                        List.of("A", "A", "B")),
                        BigDecimal.valueOf(26000), Map.of(
                        "A", List.of("same_symbol_5_times", "same_symbols_vertically"),
                        "B", List.of("same_symbol_3_times", "same_symbols_vertically")),
                        "+1000")},

                {"3x3_2", 100, new String[][]{
                        {"A", "A", "B"},
                        {"A", "+1000", "B"},
                        {"A", "A", "B"},
                }, new Output(List.of(
                        List.of("A", "A", "B"),
                        List.of("A", "+1000", "B"),
                        List.of("A", "A", "B")),
                        BigDecimal.valueOf(6600), Map.of(
                        "A", List.of("same_symbol_5_times", "same_symbols_vertically"),
                        "B", List.of("same_symbol_3_times", "same_symbols_vertically")),
                        "+1000")},

                {"3x3_1", 100, new String[][]{
                        {"A", "B", "C"},
                        {"E", "B", "5x"},
                        {"F", "D", "C"},
                }, new Output(List.of(
                        List.of("A", "B", "C"),
                        List.of("E", "B", "5x"),
                        List.of("F", "D", "C")),
                        BigDecimal.ZERO, null, null)},

                {"3x3_1", 100, new String[][]{
                        {"A", "B", "C"},
                        {"E", "B", "10x"},
                        {"F", "D", "B"},
                }, new Output(List.of(
                        List.of("A", "B", "C"),
                        List.of("E", "B", "10x"),
                        List.of("F", "D", "B")),
                        BigDecimal.valueOf(25000), Map.of(
                        "B", List.of("same_symbol_3_times")),
                        "10x")},

                {"3x3_1", 0.01, new String[][]{
                        {"A", "B", "C"},
                        {"E", "B", "10x"},
                        {"F", "D", "B"},
                }, new Output(List.of(
                        List.of("A", "B", "C"),
                        List.of("E", "B", "10x"),
                        List.of("F", "D", "B")),
                        BigDecimal.valueOf(2.5), Map.of(
                        "B", List.of("same_symbol_3_times")),
                        "10x")},

                {"2x2_1", 1, new String[][]{
                        {"A", "A"},
                        {"A", "+1000"},
                }, new Output(List.of(
                        List.of("A", "A"),
                        List.of("A", "+1000")),
                        BigDecimal.valueOf(1050), Map.of(
                        "A", List.of("same_symbol_3_times")),
                        "+1000")},

                {"5x10_1", 1, new String[][]{
                        {"A", "A", "C", "D", "E", "F", "MISS", "C", "A", "A"},
                        {"A", "A", "C", "D", "E", "F", "B", "A", "A", "A"},
                        {"F", "B", "A", "D", "E", "F", "A", "C", "D", "E"},
                        {"A", "A", "C", "A", "E", "A", "B", "C", "A", "A"},
                        {"A", "A", "C", "D", "A", "F", "B", "C", "A", "A"},
                }, new Output(List.of(
                        List.of("A", "A", "C", "D", "E", "F", "MISS", "C", "A", "A"),
                        List.of("A", "A", "C", "D", "E", "F", "B", "A", "A", "A"),
                        List.of("F", "B", "A", "D", "E", "F", "A", "C", "D", "E"),
                        List.of("A", "A", "C", "A", "E", "A", "B", "C", "A", "A"),
                        List.of("A", "A", "C", "D", "A", "F", "B", "C", "A", "A")),
                        BigDecimal.valueOf(250000), Map.of(
                        "A", List.of("corners", "zig_zag")),
                        null)},
        };
    }

    @SneakyThrows
    @MethodSource("checkData")
    @ParameterizedTest
    void checkTest(String configFileName, double betAmount, String[][] array, Output expectedOutput) {
        // Given
        URL resource = this.getClass().getClassLoader().getResource("scratch_game_checker_test/" + configFileName + ".json");
        ScratchGameConfiguration scratchGameConfiguration = new ObjectMapper().readValue(resource, ScratchGameConfiguration.class);
        ScratchGameChecker gameChecker = new ScratchGameChecker(scratchGameConfiguration);
        Matrix matrix = new Matrix(array);
        Input input = new Input(BigDecimal.valueOf(betAmount));

        // When
        Output actualOutput = gameChecker.check(matrix, input);

        // Then
        assertThat(actualOutput)
                .usingComparatorForType(BIG_DECIMAL_COMPARATOR, BigDecimal.class)
                .usingRecursiveComparison()
                .isEqualTo(expectedOutput);
    }
}