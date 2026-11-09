package org.jenkinsci.plugins.scm_filter.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Matches complete SCM head names against newline-delimited glob patterns. */
public final class RetainedRefMatcher {

    private final List<Pattern> patterns;

    public RetainedRefMatcher(String retainedRefPatterns) {
        patterns = retainedRefPatterns == null
                ? List.of()
                : Arrays.stream(retainedRefPatterns.split("\\R"))
                        .map(String::trim)
                        .filter(pattern -> !pattern.isEmpty())
                        .map(RetainedRefMatcher::compileGlob)
                        .toList();
    }

    public boolean matches(String refName) {
        return refName != null
                && patterns.stream()
                        .anyMatch(pattern -> pattern.matcher(refName).matches());
    }

    private static Pattern compileGlob(String glob) {
        StringBuilder regex = new StringBuilder();
        StringBuilder literal = new StringBuilder();

        for (int index = 0; index < glob.length(); index++) {
            char character = glob.charAt(index);
            if (character == '*' || character == '?') {
                appendLiteral(regex, literal);
                regex.append(character == '*' ? ".*" : ".");
            } else {
                literal.append(character);
            }
        }
        appendLiteral(regex, literal);
        return Pattern.compile(regex.toString());
    }

    private static void appendLiteral(StringBuilder regex, StringBuilder literal) {
        if (!literal.isEmpty()) {
            regex.append(Pattern.quote(literal.toString()));
            literal.setLength(0);
        }
    }
}
