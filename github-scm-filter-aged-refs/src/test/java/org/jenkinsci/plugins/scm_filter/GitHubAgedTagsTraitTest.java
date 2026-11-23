package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
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
                .hasFieldOrPropertyWithValue("retainedRefsRegex", null);
    }

    @Test
    void regexKeepsOnlyMatchingOldTags() throws Exception {
        GitHubAgedTagsTrait trait = new GitHubAgedTagsTrait("30");
        trait.setRetainedRefsRegex("v1\\..*");
        long old = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60);
        SCMHeadFilter filter = filter(trait);
        GitHubSCMSourceRequest request = mock(GitHubSCMSourceRequest.class);
        assertThat(filter.isExcluded(request, new GitHubTagSCMHead("v1.0", old)))
                .isFalse();
        assertThat(filter.isExcluded(request, new GitHubTagSCMHead("v2.0", old)))
                .isTrue();
    }

    private static SCMHeadFilter filter(GitHubAgedTagsTrait trait) {
        GitHubSCMSourceContext context = new GitHubSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(context);
        return context.filters().get(0);
    }
}
