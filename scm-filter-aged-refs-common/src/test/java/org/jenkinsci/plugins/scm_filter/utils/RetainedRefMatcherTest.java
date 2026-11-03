package org.jenkinsci.plugins.scm_filter.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RetainedRefMatcherTest {

    @Test
    void emptyConfigurationMatchesNothing() {
        assertThat(new RetainedRefMatcher(null).matches("main")).isFalse();
        assertThat(new RetainedRefMatcher("").matches("main")).isFalse();
        assertThat(new RetainedRefMatcher("  \n\t").matches("main")).isFalse();
    }

    @Test
    void matchesExactAndWildcardPatterns() {
        RetainedRefMatcher matcher = new RetainedRefMatcher("main\nrelease/*\nPR-?\nv1.*");

        assertThat(matcher.matches("main")).isTrue();
        assertThat(matcher.matches("release/1.0")).isTrue();
        assertThat(matcher.matches("release/archive/1.0")).isTrue();
        assertThat(matcher.matches("PR-1")).isTrue();
        assertThat(matcher.matches("PR-10")).isFalse();
        assertThat(matcher.matches("v1.2.3")).isTrue();
    }

    @Test
    void matchingIsCaseSensitiveAndCoversTheCompleteName() {
        RetainedRefMatcher matcher = new RetainedRefMatcher("main");

        assertThat(matcher.matches("Main")).isFalse();
        assertThat(matcher.matches("feature/main")).isFalse();
        assertThat(matcher.matches("main-old")).isFalse();
    }

    @Test
    void ignoresWhitespaceAndCrLfLines() {
        RetainedRefMatcher matcher = new RetainedRefMatcher("  main  \r\n\r\n release/* \r\n");

        assertThat(matcher.matches("main")).isTrue();
        assertThat(matcher.matches("release/1.0")).isTrue();
    }

    @Test
    void treatsRegularExpressionCharactersLiterally() {
        RetainedRefMatcher matcher = new RetainedRefMatcher("release/[1].(x)+");

        assertThat(matcher.matches("release/[1].(x)+")).isTrue();
        assertThat(matcher.matches("release/1.xxx")).isFalse();
    }
}
