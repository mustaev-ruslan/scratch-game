package org.scratchgame.domain;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.stream;

public record Matrix(String[][] array) {

    public List<List<String>> toList() {
        return stream(array).map(Arrays::asList).toList();
    }
}
