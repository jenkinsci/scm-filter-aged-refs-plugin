package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
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
                .hasFieldOrPropertyWithValue("retainedRefsRegex", null);
    }

    @Test
    void regexKeepsPullRequestWithoutLookingItUp() throws Exception {
        GitHubAgedPullRequestsTrait trait = new GitHubAgedPullRequestsTrait("30");
        trait.setRetainedRefsRegex("PR-1");
        PullRequestSCMHead head = mock(PullRequestSCMHead.class);
        when(head.getName()).thenReturn("PR-1");
        GitHubSCMSourceRequest request = mock(GitHubSCMSourceRequest.class);
        assertThat(filter(trait).isExcluded(request, head)).isFalse();
        verifyNoInteractions(request);
    }

    private static SCMHeadFilter filter(GitHubAgedPullRequestsTrait trait) {
        GitHubSCMSourceContext context = new GitHubSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(context);
        return context.filters().get(0);
    }
}
