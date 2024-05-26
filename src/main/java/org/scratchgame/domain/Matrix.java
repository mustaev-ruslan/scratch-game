package org.scratchgame.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.stream;

@RequiredArgsConstructor
public final class Matrix implements Iterable<String> {
    private final String[][] array;

    public List<List<String>> toList() {
        return stream(array).map(Arrays::asList).toList();
    }

    @Override
    public Iterator<String> iterator() {
        return stream(array).flatMap(Arrays::stream).iterator();
    }

    public List<String> getValues(List<Cell> cells) {
        return cells.stream()
                .map(it -> array[it.row()][it.column()])
                .toList();
    }
}
