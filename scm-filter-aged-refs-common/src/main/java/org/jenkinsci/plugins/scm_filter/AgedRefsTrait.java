package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import java.io.IOException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.jenkinsci.plugins.scm_filter.utils.FormValidationUtils;
import org.jenkinsci.plugins.scm_filter.utils.RetainedRefMatcher;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public abstract class AgedRefsTrait extends SCMSourceTrait {

    final int retentionDays;
    private String retainedRefPatterns = "";

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     */
    protected AgedRefsTrait(String retentionDays) {
        this.retentionDays = Integer.parseInt(retentionDays);
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getRetentionDays() {
        return this.retentionDays;
    }

    public String getRetainedRefPatterns() {
        return retainedRefPatterns == null ? "" : retainedRefPatterns;
    }

    @DataBoundSetter
    public void setRetainedRefPatterns(String retainedRefPatterns) {
        this.retainedRefPatterns = retainedRefPatterns == null ? "" : retainedRefPatterns;
    }

    @Override
    protected abstract void decorateContext(SCMSourceContext<?, ?> context);

    /**
     * Our descriptor.
     */
    abstract static class AgedRefsDescriptorImpl extends SCMSourceTraitDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return "Filter by ref age";
        }

        @Restricted(NoExternalUse.class)
        @POST
        public FormValidation doCheckRetentionDays(@QueryParameter String value) {
            return FormValidationUtils.checkRetentionDays(value);
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined retentionDays.
     */
    public abstract static class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {

        private final long acceptableDateTimeThreshold;
        private final RetainedRefMatcher retainedRefMatcher;

        protected ExcludeBranchesSCMHeadFilter(int retentionDays) {
            this(retentionDays, "");
        }

        protected ExcludeBranchesSCMHeadFilter(int retentionDays, String retainedRefPatterns) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
            retainedRefMatcher = new RetainedRefMatcher(retainedRefPatterns);
        }

        public long getAcceptableDateTimeThreshold() {
            return acceptableDateTimeThreshold;
        }

        protected boolean isRetainedRef(SCMHead scmHead) {
            return retainedRefMatcher.matches(scmHead.getName());
        }

        @Override
        public abstract boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException;
    }
}
