package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubTagSCMHead;
import org.junit.jupiter.api.Test;

class GitHubAgedTagsTraitTest {

    private GitHubSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData() throws IOException {
        GitHubSCMSource instance = load("exclude_tags_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedTagsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefPatterns", "");
    }

    @Test
    void retainedTagBypassesAgeLookup() throws IOException, InterruptedException {
        GitHubAgedTagsTrait.ExcludeOldTagsSCMHeadFilter filter =
                new GitHubAgedTagsTrait.ExcludeOldTagsSCMHeadFilter(30, "v1.*");

        assertThat(filter.isExcluded(null, new GitHubTagSCMHead("v1.0", 0))).isFalse();
    }
}
