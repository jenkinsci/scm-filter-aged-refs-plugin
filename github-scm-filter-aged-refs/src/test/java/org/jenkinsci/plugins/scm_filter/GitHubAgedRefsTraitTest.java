package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

class GitHubAgedRefsTraitTest {

    private GitHubSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData() throws IOException {
        GitHubSCMSource instance = load("exclude_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30);
    }

    @Test
    void checkFilterTagsSetting() throws IOException {
        GitHubSCMSource instance = load("exclude_ten_days_only_tags.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 10)
                .hasFieldOrPropertyWithValue("filterTags", false);
    }
}
