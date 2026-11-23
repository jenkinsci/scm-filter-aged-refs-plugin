package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.PatternSyntaxException;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
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
                .hasFieldOrPropertyWithValue("retainDefaultBranch", false)
                .hasFieldOrPropertyWithValue("retainedRefsRegex", null)
                .hasFieldOrPropertyWithValue("retainedRefsExcludeRegex", null);
    }

    @Test
    void excludeRegexOverridesKeepRegex() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainedRefsRegex("release/.*");
        trait.setRetainedRefsExcludeRegex("release/old-.*");
        SCMHeadFilter filter = filter(trait);
        GitHubSCMSourceRequest kept = GitHubTestRequests.withOldBranch("release/2.0", "main");
        assertThat(filter.isExcluded(kept, new BranchSCMHead("release/2.0"))).isFalse();
        GitHubSCMSourceRequest excepted = GitHubTestRequests.withOldBranch("release/old-1", "main");
        assertThat(filter.isExcluded(excepted, new BranchSCMHead("release/old-1")))
                .isTrue();
    }

    @Test
    void excludeRegexOverridesDefaultBranch() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainDefaultBranch(true);
        trait.setRetainedRefsExcludeRegex("main");
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isTrue();
    }

    @Test
    void invalidExcludeRegexIsRejected() {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        assertThatThrownBy(() -> trait.setRetainedRefsExcludeRegex("*old")).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    void regexKeepsOldBranchWithoutDefaultBranchOption() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainedRefsRegex("main|master");
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isFalse();
        verify(request, never()).getBranches();
    }

    @Test
    void regexDoesNotKeepOtherBranches() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainedRefsRegex("main|master");
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("feature", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("feature")))
                .isTrue();
    }

    @Test
    void invalidRegexIsRejected() {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        assertThatThrownBy(() -> trait.setRetainedRefsRegex("release/(")).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    void blankRegexMeansNone() {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainedRefsRegex("  ");
        assertThat(trait.getRetainedRefsRegex()).isNull();
    }

    @Test
    void oldDefaultBranchIsExcludedByDefault() throws Exception {
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        assertThat(filter(new GitHubAgedBranchesTrait("30")).isExcluded(request, new BranchSCMHead("main")))
                .isTrue();
    }

    @Test
    void oldDefaultBranchIsAcceptedWhenRetained() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainDefaultBranch(true);
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isFalse();
        verify(request, never()).getBranches();
    }

    @Test
    void otherOldBranchIsExcludedWhenRetained() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainDefaultBranch(true);
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("feature", "main");
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("feature")))
                .isTrue();
    }

    @Test
    void oldBranchIsExcludedWhenRepositoryIsUnknown() throws Exception {
        GitHubAgedBranchesTrait trait = new GitHubAgedBranchesTrait("30");
        trait.setRetainDefaultBranch(true);
        GitHubSCMSourceRequest request = GitHubTestRequests.withOldBranch("main", "main");
        when(request.getRepository()).thenReturn(null);
        assertThat(filter(trait).isExcluded(request, new BranchSCMHead("main"))).isTrue();
    }

    private static SCMHeadFilter filter(GitHubAgedBranchesTrait trait) {
        GitHubSCMSourceContext context = new GitHubSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(context);
        return context.filters().get(0);
    }
}
