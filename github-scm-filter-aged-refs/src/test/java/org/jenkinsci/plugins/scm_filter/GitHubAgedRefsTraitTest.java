package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
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
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainDefaultBranch", false)
                .hasFieldOrPropertyWithValue("retainedRefsRegex", null);
    }

    @Test
    void regexKeepsOldBranch() throws Exception {
        GitHubAgedRefsTrait trait = new GitHubAgedRefsTrait("30");
        trait.setRetainedRefsRegex("release/.*");
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("release/1.0", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("release/1.0")))
                .isFalse();
    }

    @Test
    void oldDefaultBranchIsAcceptedWhenRetained() throws Exception {
        GitHubAgedRefsTrait trait = new GitHubAgedRefsTrait("30");
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isTrue();
        trait.setRetainDefaultBranch(true);
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isFalse();
    }

    private static SCMHeadFilter filter(GitHubAgedRefsTrait trait) {
        GitHubSCMSourceContext context = new GitHubSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(context);
        return context.filters().get(0);
    }
}
