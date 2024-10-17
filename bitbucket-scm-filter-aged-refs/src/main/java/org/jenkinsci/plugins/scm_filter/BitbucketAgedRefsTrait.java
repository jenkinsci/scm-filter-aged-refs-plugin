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

/**
 * @author witokondoria
 */
public class BitbucketAgedRefsTrait extends AgedRefsTrait {

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     * @param filterBranches if filter should be applied to branches
     * @param filterPullRequests if filter should be applied to pull requests
     * @param filterTags if filter should be applied to tags
     */
    @DataBoundConstructor
    public BitbucketAgedRefsTrait(
            String retentionDays, String filterBranches, String filterPullRequests, String filterTags) {
        super(retentionDays, filterBranches, filterPullRequests, filterTags);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        if (retentionDays > 0) {
            context.withFilter(
                    new ExcludeOldBranchesSCMHeadFilter(retentionDays, filterBranches, filterPullRequests, filterTags));
        }
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
     * Filter that excludes references (branches, pull requests and/or tags) according to their last commit modification date and the defined retentionDays.
     */
    private static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        ExcludeOldBranchesSCMHeadFilter(
                int retentionDays, boolean filterBranches, boolean filterPullRequests, boolean filterTags) {
            super(retentionDays, filterBranches, filterPullRequests, filterTags);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException {
            if (super.shouldFilterBranches() && scmHead instanceof BranchSCMHead) {
                Iterable<BitbucketBranch> branches = ((BitbucketSCMSourceRequest) scmSourceRequest).getBranches();
                for (BitbucketBranch branch : branches) {
                    long branchTS = branch.getDateMillis();
                    if (branch.getName().equals(scmHead.getName())) {
                        return branchTS < super.getAcceptableDateTimeThreshold();
                    }
                }
            } else if (super.shouldFilterPullRequest() && scmHead instanceof PullRequestSCMHead) {
                Iterable<BitbucketPullRequest> pulls = ((BitbucketSCMSourceRequest) scmSourceRequest).getPullRequests();
                for (BitbucketPullRequest pull : pulls) {
                    if (pull.getSource().getBranch().getName().equals(scmHead.getName())) {
                        long pullTS = pull.getSource().getCommit().getDateMillis();
                        return pullTS < super.getAcceptableDateTimeThreshold();
                    }
                }
            } else if (super.shouldFilterTags() && scmHead instanceof BitbucketTagSCMHead) {
                long tagTS = ((BitbucketTagSCMHead) scmHead).getTimestamp();
                return tagTS < super.getAcceptableDateTimeThreshold();
            }
            return false;
        }
    }
}
