package org.isf.exa.gui;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.swing.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class ExamFilterFactoryTest {
    private ExamFilterFactory examFilterFactory = new ExamFilterFactory();

    @Test
    public void shouldBuildFiltersForAllWordsProvided() {
        // given:
        String text = "dupa kupa siki";

        // when:
        List<RowFilter<Object, Object>> rowFilters = examFilterFactory.buildFilters(text);

        // then
        assertThat(rowFilters.size()).isEqualTo(3);
    }

    @Test
    public void shouldReturnEmptyForEmptyText() {
        // given:
        String text = "";

        // when:
        List<RowFilter<Object, Object>> rowFilters = examFilterFactory.buildFilters(text);

        // then
        assertThat(rowFilters.size()).isEqualTo(0);
    }

}