package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.io.IOException;
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
import org.kohsuke.stapler.DataBoundConstructor;

public class GitHubAgedRefsTrait extends AgedRefsTrait {

    /**
     * Constructor for stapler.
     *
     * @param branchRetentionDays retention period in days for branches
     * @param prRetentionDays     retention period in days for pull requests
     * @param tagRetentionDays    retention period in days for tags
     * @param branchExcludeFilter space-separated list of branch name patterns to
     *                            ignore. For example: release main hotfix-*
     */
    @DataBoundConstructor
    public GitHubAgedRefsTrait(
            String branchRetentionDays, String prRetentionDays, String tagRetentionDays, String branchExcludeFilter) {
        super(branchRetentionDays, prRetentionDays, tagRetentionDays, branchExcludeFilter);
    }

    protected transient String retentionDays;

    protected Object readResolve() {
        if (retentionDays != null || !retentionDays.isEmpty()) {
            super.branchRetentionDays = Integer.parseInt(retentionDays);
            super.prRetentionDays = Integer.parseInt(retentionDays);
            super.tagRetentionDays = Integer.parseInt(retentionDays);
        }
        return this;
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withFilter(new ExcludeOldBranchesSCMHeadFilter(
                branchRetentionDays, prRetentionDays, tagRetentionDays, branchExcludeFilter));
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
     * Filter that excludes references (branches, pull requests, tags) according to
     * their last commit modification date and the defined branchRetentionDays.
     */
    private static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        ExcludeOldBranchesSCMHeadFilter(
                int branchRetentionDays, int prRetentionDays, int tagRetentionDays, String branchExcludeFilter) {
            super(branchRetentionDays, prRetentionDays, tagRetentionDays, branchExcludeFilter);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead && super.getAcceptableBranchDateTimeThreshold() > 0) {
                if (scmHead.getName().matches(super.getBranchExcludePattern())) {
                    return false;
                }

                Iterable<GHBranch> branches = ((GitHubSCMSourceRequest) scmSourceRequest).getBranches();
                for (GHBranch branch : branches) {
                    if (branch.getName().equals(scmHead.getName())) {
                        long branchTS = branch.getOwner()
                                .getCommit(branch.getSHA1())
                                .getCommitDate()
                                .getTime();
                        return branchTS < super.getAcceptableBranchDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead && super.getAcceptableBranchDateTimeThreshold() > 0) {
                Iterable<GHPullRequest> pulls = ((GitHubSCMSourceRequest) scmSourceRequest).getPullRequests();
                for (GHPullRequest pull : pulls) {
                    if (("PR-" + pull.getNumber()).equals(scmHead.getName())) {
                        long pullTS = pull.getHead()
                                .getCommit()
                                .getCommitShortInfo()
                                .getCommitDate()
                                .getTime();
                        return pullTS < super.getAcceptablePRDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof GitHubTagSCMHead && super.getAcceptableBranchDateTimeThreshold() > 0) {
                long tagTS = ((GitHubTagSCMHead) scmHead).getTimestamp();
                return tagTS < super.getAcceptableTagDateTimeThreshold();
            }
            return false;
        }
    }
}
