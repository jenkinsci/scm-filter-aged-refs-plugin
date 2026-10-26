package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

class GitHubAgedBranchesTraitTest {

    private GitHubSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData() throws IOException {
        GitHubSCMSource instance = load("exclude_branches_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedBranchesTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefPatterns", "");
    }

    @Test
    void restoreRetainedRefPatterns() throws IOException {
        GitHubSCMSource instance = load("retain_selected_branches.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedBranchesTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefPatterns", "main\nrelease/*");
    }

    @Test
    void retainedRefBypassesAgeLookup() throws IOException, InterruptedException {
        GitHubAgedBranchesTrait.ExcludeOldBranchesSCMHeadFilter filter =
                new GitHubAgedBranchesTrait.ExcludeOldBranchesSCMHeadFilter(30, "main");

        assertThat(filter.isExcluded(null, new org.jenkinsci.plugins.github_branch_source.BranchSCMHead("main")))
                .isFalse();
    }
}
