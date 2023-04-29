package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.gitlabbranchsource.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.impl.trait.Selection;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.MergeRequest;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class GitLabAgedRefsTrait extends AgedRefsTrait {
    public static final Logger LOGGER = Logger.getLogger(GitLabAgedRefsTrait.class.getName());

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     */
    @DataBoundConstructor
    public GitLabAgedRefsTrait(String retentionDays) {
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
    @Symbol("gitLabAgedRefsTrait")
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends AgedRefsDescriptorImpl {

        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitLabSCMSourceContext.class;
        }

        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GitLabSCMSource.class;
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
                throws IOException {
            if (scmHead instanceof BranchSCMHead) {
                Iterable<Branch> branches = ((GitLabSCMSourceRequest) scmSourceRequest).getBranches();
                for (Branch branch : branches) {
                    if (branch.getName().equals(scmHead.getName())) {
                        long branchTS = branch.getCommit().getCommittedDate().getTime();
                        return branchTS < getAcceptableDateTimeThreshold();
                    }
                }
            } else if (scmHead instanceof MergeRequestSCMHead) {
                MergeRequestSCMHead mrHead = (MergeRequestSCMHead) scmHead;
                GitLabSCMSourceRequest gitLabSCMSourceRequest = (GitLabSCMSourceRequest) scmSourceRequest;
                Iterable<MergeRequest> mrs = gitLabSCMSourceRequest.getMergeRequests();
                for (MergeRequest mr : mrs) {
                    if (Long.toString(mr.getId()).equals(mrHead.getId())) {
                        return isMrExcluded(gitLabSCMSourceRequest, mr);
                    }
                }
            }
            return super.isExcluded(scmSourceRequest, scmHead);
        }

        private boolean isMrExcluded(GitLabSCMSourceRequest gitLabSCMSourceRequest, MergeRequest mr) {
            GitLabApi api = gitLabSCMSourceRequest.getGitLabApi();
            if (api == null) {
                LOGGER.log(Level.FINEST, "No GitLab API?!?");
                return false;
            }
            try {
                Commit commit = api.getCommitsApi().getCommit(mr.getSourceProjectId(), mr.getSha());
                long pullTS = commit.getCommittedDate().getTime();
                return pullTS < getAcceptableDateTimeThreshold();
            } catch (GitLabApiException e) {
                LOGGER.log(Level.FINE, e, () -> "Cannot resolve commit " + mr.getSha() + " for MR " + mr.getId());
                return false;
            }
        }
    }
}
