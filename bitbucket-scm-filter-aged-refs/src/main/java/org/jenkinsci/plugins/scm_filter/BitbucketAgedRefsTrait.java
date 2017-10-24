package org.jenkinsci.plugins.scm_filter;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author witokondoria
 */
public class BitbucketAgedRefsTrait extends AgedRefsTrait{

    /**
     * Constructor for stapler.
     *
     * @param retentionDays
     */
    public BitbucketAgedRefsTrait(String retentionDays) {
        super(retentionDays);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        if (this.retentionDays > 0) {
            context.withFilter(new BitbucketAgedRefsTrait.ExcludeOldBranchesSCMHeadFilter(this.retentionDays));
        }
    }

    /**
     * Our descriptor.
     */
    @Extension
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends AgedRefsTrait.AgedRefsDescriptorImpl {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return super.getDisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicableToBuilder(@NonNull Class<? extends SCMBuilder> builderClass) {
            return BitbucketGitSCMBuilder.class.isAssignableFrom(builderClass);
        }

    }

    /**
     * Filter that excludes references (branches or pull requests) according to its last commit modification date and the defined retentionDays.
     */
    public static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter {

        public ExcludeOldBranchesSCMHeadFilter(int retentionDays) {
            super(retentionDays);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead) {
                Iterable<BitbucketBranch> branches = ((BitbucketSCMSourceRequest) scmSourceRequest).getBranches();
                Iterator<BitbucketBranch> branchIterator = branches.iterator();
                while (branchIterator.hasNext()) {
                    BitbucketBranch branch = branchIterator.next();
                    long branchTS = branch.getDateMillis();
                    if (branch.getName().equals(scmHead.getName())) {
                        return (Long.compare(branchTS, super.getAcceptableDateTimeThreshold()) < 0);
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead) {
                Iterable<BitbucketPullRequest> pulls = ((BitbucketSCMSourceRequest) scmSourceRequest).getPullRequests();
                Iterator<BitbucketPullRequest> pullIterator = pulls.iterator();
                while (pullIterator.hasNext()) {
                    BitbucketPullRequest pull = pullIterator.next();
                    if (pull.getSource().getBranch().getName().equals(scmHead.getName())) {
                        long pullTS = pull.getSource().getCommit().getDateMillis();
                        return (Long.compare(pullTS, super.getAcceptableDateTimeThreshold()) < 0);
                    }
                }
            }
            return false;
        }
    }
}
