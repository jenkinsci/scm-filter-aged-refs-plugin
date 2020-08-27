package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.jenkinsci.Symbol;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMBuilder;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.GitHubTagSCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHTagObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Iterator;

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
        public String getDisplayName() {
            return super.getDisplayName();
        }

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
    public static class ExcludeOldBranchesSCMHeadFilter extends ExcludeBranchesSCMHeadFilter{

        public ExcludeOldBranchesSCMHeadFilter(int retentionDays) {
            super(retentionDays);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException {
            if (scmHead instanceof BranchSCMHead) {
                Iterable<GHBranch> branches = ((GitHubSCMSourceRequest) scmSourceRequest).getBranches();
                Iterator<GHBranch> branchIterator = branches.iterator();
                while (branchIterator.hasNext()) {
                    GHBranch branch = branchIterator.next();
                    long branchTS = branch.getOwner().getCommit(branch.getSHA1()).getCommitDate().getTime();
                    if (branch.getName().equals(scmHead.getName())) {
                        return (Long.compare(branchTS, super.getAcceptableDateTimeThreshold()) < 0);
                    }
                }
            } else if (scmHead instanceof PullRequestSCMHead) {
                Iterable<GHPullRequest> pulls = ((GitHubSCMSourceRequest) scmSourceRequest).getPullRequests();
                Iterator<GHPullRequest> pullIterator = pulls.iterator();
                while (pullIterator.hasNext()) {
                    GHPullRequest pull = pullIterator.next();
                    if (("PR-" + pull.getNumber()).equals(scmHead.getName())) {
                        long pullTS = pull.getHead().getCommit().getCommitShortInfo().getCommitDate().getTime();
                        return (Long.compare(pullTS, super.getAcceptableDateTimeThreshold()) < 0);
                    }
                }
            } else if (scmHead instanceof GitHubTagSCMHead) {
                Iterable<GHRef> refs = ((GitHubSCMSourceRequest) scmSourceRequest).getTags();
                Iterator<GHRef> refIterator = refs.iterator();
                while (refIterator.hasNext()) {
                    GHRef ref = refIterator.next();
                    String sha = ref.getObject().getSha();
                    long refTS = 0L;
                    if ("tag".equalsIgnoreCase(ref.getObject().getType())) {
                        // annotated tag object
                        try {
                            GHTagObject tagObject = ((GitHubSCMSourceRequest) scmSourceRequest).getRepository().getTagObject(sha);
                            refTS = tagObject.getTagger().getDate().getTime();
                        } catch (IOException e) {
                            // ignore, if the tag doesn't exist, the probe will handle that correctly
                            // we just need enough of a date value to allow for probing
                        }
                    } else {
                        try {
                            GHCommit commit = ((GitHubSCMSourceRequest) scmSourceRequest).getRepository().getCommit(sha);
                            refTS = commit.getCommitDate().getTime();
                        } catch (IOException e) {
                            // ignore, if the tag doesn't exist, the probe will handle that correctly
                            // we just need enough of a date value to allow for probing
                        }
                    }
                    if (ref.getRef().equals("refs/tags/" + scmHead.getName())) {
                        return (Long.compare(refTS, super.getAcceptableDateTimeThreshold()) < 0);
                    }
                }
            }
            return false;
        }
    }
}
