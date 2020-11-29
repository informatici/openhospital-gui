package org.isf.exa.gui;

import javax.swing.RowFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class ExamFilterFactory {
    public List<RowFilter<Object, Object>> buildFilters(String s) {
        try {
            return Arrays.stream(s.split(" "))
                    .filter(token-> !token.isEmpty())
                    .map(String::toLowerCase)
                    .map(token -> RowFilter.regexFilter("(?i)" + token))
                    .collect(Collectors.toList());
        } catch (PatternSyntaxException pse) {
            System.out.println("Bad regex pattern");
            return Collections.emptyList();
        }
    }
}
