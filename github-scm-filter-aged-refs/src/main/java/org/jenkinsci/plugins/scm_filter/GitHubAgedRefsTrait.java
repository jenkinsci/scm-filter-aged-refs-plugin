package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.impl.trait.Selection;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.GitHubTagSCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author witokondoria
 */
public class GitHubAgedRefsTrait extends AgedRefsTrait {

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     */
    @DataBoundConstructor
    public GitHubAgedRefsTrait(String retentionDays) {
        super(retentionDays);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        if (retentionDays > 0) {
            context.withFilter(new ExcludeOldBranchesSCMHeadFilter(retentionDays));
        }
    }

    /**
     * Our descriptor.
     */
    @Extension
    @Selection
    @Symbol("gitHubAgedRefsTrait")
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends AgedRefsDescriptorImpl {

        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitHubSCMSourceContext.class;
        }

        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GitHubSCMSource.class;
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined retentionDays.
     */
    private static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        ExcludeOldBranchesSCMHeadFilter(int retentionDays) {
            super(retentionDays);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead) {
                Iterable<GHBranch> branches = ((GitHubSCMSourceRequest) scmSourceRequest).getBranches();
                for (GHBranch branch : branches) {
                    long branchTS = branch.getOwner()
                            .getCommit(branch.getSHA1())
                            .getCommitDate()
                            .getTime();
                    if (branch.getName().equals(scmHead.getName())) {
                        return branchTS < super.getAcceptableDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead) {
                int pullNr = ((PullRequestSCMHead) scmHead).getNumber();
                Iterable<GHPullRequest> pulls = ((GitHubSCMSourceRequest) scmSourceRequest).getPullRequests();
                Optional<GHPullRequest> pull = StreamSupport.stream(pulls.spliterator(), false)
                        .filter(p -> pullNr == p.getNumber())
                        .findAny();
                if (pull.isPresent()) {
                    PagedIterator<GHPullRequestCommitDetail> iterator =
                            pull.get().listCommits().withPageSize(1).iterator();
                    // Has at least one commit
                    if (iterator.hasNext()) {
                        long pullTS = iterator.next()
                                .getCommit()
                                .getCommitter()
                                .getDate()
                                .getTime();
                        return pullTS < super.getAcceptableDateTimeThreshold();
                    }
                }
                return false;
            } else if (scmHead instanceof GitHubTagSCMHead) {
                long tagTS = ((GitHubTagSCMHead) scmHead).getTimestamp();
                return tagTS < super.getAcceptableDateTimeThreshold();
            }
            return false;
        }
    }
}
