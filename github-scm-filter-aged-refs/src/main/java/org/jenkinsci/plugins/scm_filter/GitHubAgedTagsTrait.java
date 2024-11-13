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
import org.jenkinsci.plugins.github_branch_source.*;
import org.jenkinsci.plugins.scm_filter.enums.RefType;
import org.jenkinsci.plugins.scm_filter.utils.FilterRefUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class GitHubAgedTagsTrait extends AgedTypeRefsTrait {
    private static final RefType REF_TYPE = RefType.TAG;

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     */
    @DataBoundConstructor
    public GitHubAgedTagsTrait(String retentionDays) {
        super(retentionDays);
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        if (retentionDays > 0) {
            context.withFilter(new ExcludeOldTagsSCMHeadFilter(retentionDays));
        }
    }

    @Extension
    @Selection
    @Symbol("gitHubAgedTagsTrait")
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

        @Override
        @NonNull
        public String getDisplayName() {
            return "Filter tags by age";
        }

        @Override
        @NonNull
        public String getRefName() {
            return REF_TYPE.getName();
        }
    }

    /**
     * Filter that excludes tags according to their last commit modification date and the defined retentionDays.
     */
    private static class ExcludeOldTagsSCMHeadFilter extends ExcludeReferencesSCMHeadFilter {

        ExcludeOldTagsSCMHeadFilter(int retentionDays) {
            super(retentionDays);
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException {
            if (scmHead instanceof GitHubTagSCMHead) {
                return FilterRefUtils.isTagExcluded((GitHubTagSCMHead) scmHead, super.getAcceptableDateTimeThreshold());
            }
            return false;
        }
    }
}
