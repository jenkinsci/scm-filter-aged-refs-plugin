package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMBuilder;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.GitHubTagSCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * @author witokondoria
 */
public class GitHubAgedRefsTrait extends AgedRefsTrait {

    /**
     * Constructor for stapler.
     *
     * @param retentionDays
     */
    @DataBoundConstructor
    public GitHubAgedRefsTrait(String retentionDays) {
        super(retentionDays);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        if (retentionDays > 0) {
            context.withFilter(new GitHubAgedRefsTrait.ExcludeOldBranchesSCMHeadFilter(retentionDays));
        }
    }
    /**
     * Our descriptor.
     */
    @Extension @Symbol("gitHubAgedRefsTrait")
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends AgedRefsDescriptorImpl {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicableToBuilder(@NonNull Class<? extends SCMBuilder> builderClass) {
            return GitHubSCMBuilder.class.isAssignableFrom(builderClass);
        }
    }

    /**
     * Filter that excludes references (branches or pull requests) according to its last commit modification date and the defined retentionDays.
     */
    private static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        ExcludeOldBranchesSCMHeadFilter(int retentionDays) {
            super(retentionDays);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead) {
                Iterable<GHBranch> branches = ((GitHubSCMSourceRequest) scmSourceRequest).getBranches();
                for (GHBranch branch : branches) {
                    long branchTS = branch.getOwner().getCommit(branch.getSHA1()).getCommitDate().getTime();
                    if (branch.getName().equals(scmHead.getName())) {
                        return branchTS < super.getAcceptableDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead) {
                Iterable<GHPullRequest> pulls = ((GitHubSCMSourceRequest) scmSourceRequest).getPullRequests();
                for (GHPullRequest pull : pulls) {
                    if (("PR-" + pull.getNumber()).equals(scmHead.getName())) {
                        long pullTS = pull.getHead().getCommit().getCommitShortInfo().getCommitDate().getTime();
                        return pullTS < super.getAcceptableDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof GitHubTagSCMHead) {
                long refTS = ((GitHubTagSCMHead) scmHead).getTimestamp();
                return (Long.compare(refTS, super.getAcceptableDateTimeThreshold()) < 0);
            }
            return false;
        }
    }
}
