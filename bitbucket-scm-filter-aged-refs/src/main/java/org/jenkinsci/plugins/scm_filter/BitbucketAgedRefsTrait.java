package org.jenkinsci.plugins.scm_filter;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceContext;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketTagSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.io.IOException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.impl.trait.Selection;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class BitbucketAgedRefsTrait extends AgedRefsTrait {

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
    public BitbucketAgedRefsTrait(String branchRetentionDays, String prRetentionDays, String tagRetentionDays,
            String branchExcludeFilter) {
        super(branchRetentionDays, prRetentionDays, tagRetentionDays, branchExcludeFilter);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withFilter(new ExcludeOldBranchesSCMHeadFilter(branchRetentionDays, prRetentionDays, tagRetentionDays,
                branchExcludeFilter));
    }

    /**
     * Our descriptor.
     */
    @Extension
    @Selection
    @Symbol("bitbucketAgedRefsTrait")
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends AgedRefsDescriptorImpl {

        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return BitbucketSCMSourceContext.class;
        }

        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return BitbucketSCMSource.class;
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to
     * their last commit modification date and the defined branchRetentionDays.
     */
    private static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        ExcludeOldBranchesSCMHeadFilter(int branchRetentionDays, int prRetentionDays, int tagRetentionDays,
                String branchExcludeFilter) {
            super(branchRetentionDays, prRetentionDays, tagRetentionDays, branchExcludeFilter);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead && super.getAcceptableBranchDateTimeThreshold() > 0) {
                if (scmHead.getName().matches(super.getBranchExcludePattern())) {
                    return false;
                }

                Iterable<BitbucketBranch> branches = ((BitbucketSCMSourceRequest) scmSourceRequest).getBranches();
                for (BitbucketBranch branch : branches) {
                    if (branch.getName().equals(scmHead.getName())) {
                        long branchTS = branch.getDateMillis();
                        return branchTS < super.getAcceptableBranchDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead && super.getAcceptablePRDateTimeThreshold() > 0) {
                Iterable<BitbucketPullRequest> pulls = ((BitbucketSCMSourceRequest) scmSourceRequest).getPullRequests();
                for (BitbucketPullRequest pull : pulls) {
                    if (pull.getSource().getBranch().getName().equals(scmHead.getName())) {
                        long pullTS = pull.getSource().getCommit().getDateMillis();
                        return pullTS < super.getAcceptablePRDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof BitbucketTagSCMHead && super.getAcceptableTagDateTimeThreshold() > 0) {
                long tagTS = ((BitbucketTagSCMHead) scmHead).getTimestamp();
                return tagTS < super.getAcceptableTagDateTimeThreshold();
            }
            return false;
        }
    }
}
