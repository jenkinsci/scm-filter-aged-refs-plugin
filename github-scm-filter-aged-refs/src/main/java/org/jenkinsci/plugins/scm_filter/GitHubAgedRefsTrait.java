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
import org.jenkinsci.plugins.scm_filter.utils.GitHubFilterRefUtils;
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
                return GitHubFilterRefUtils.isBranchExcluded(
                        (GitHubSCMSourceRequest) scmSourceRequest,
                        (BranchSCMHead) scmHead,
                        getAcceptableDateTimeThreshold());
            } else if (scmHead instanceof PullRequestSCMHead) {
                return GitHubFilterRefUtils.isPullRequestExcluded(
                        (GitHubSCMSourceRequest) scmSourceRequest,
                        (PullRequestSCMHead) scmHead,
                        getAcceptableDateTimeThreshold());
            } else if (scmHead instanceof GitHubTagSCMHead) {
                return GitHubFilterRefUtils.isTagExcluded((GitHubTagSCMHead) scmHead, getAcceptableDateTimeThreshold());
            }
            return false;
        }
    }
}
