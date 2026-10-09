package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.junit.jupiter.api.Test;

class GitHubAgedPullRequestsTraitTest {

    private GitHubSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData() throws IOException {
        GitHubSCMSource instance = load("exclude_pull_requests_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedPullRequestsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefPatterns", "");
    }

    @Test
    void retainedPullRequestBypassesAgeLookup() throws IOException, InterruptedException {
        GitHubAgedPullRequestsTrait.ExcludeOldPullRequestsSCMHeadFilter filter =
                new GitHubAgedPullRequestsTrait.ExcludeOldPullRequestsSCMHeadFilter(30, "PR-123");
        PullRequestSCMHead head = new PullRequestSCMHead(
                "PR-123",
                "repository",
                "owner",
                "feature",
                123,
                new BranchSCMHead("main"),
                SCMHeadOrigin.DEFAULT,
                ChangeRequestCheckoutStrategy.HEAD);

        assertThat(filter.isExcluded(null, head)).isFalse();
    }
}
