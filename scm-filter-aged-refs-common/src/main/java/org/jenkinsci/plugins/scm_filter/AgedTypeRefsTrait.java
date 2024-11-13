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
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public abstract class AgedTypeRefsTrait extends SCMSourceTrait {
    final int retentionDays;

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     */
    protected AgedTypeRefsTrait(String retentionDays) {
        this.retentionDays = Integer.parseInt(retentionDays);
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getRetentionDays() {
        return this.retentionDays;
    }

    @Override
    protected abstract void decorateContext(SCMSourceContext<?, ?> context);

    abstract static class AgedRefsDescriptorImpl extends SCMSourceTraitDescriptor {

        @Override
        @NonNull
        public abstract String getDisplayName();

        public abstract String getRefName();

        @Restricted(NoExternalUse.class)
        @POST
        public FormValidation doCheckRetentionDays(@QueryParameter String value) {
            return FormValidationUtils.checkRetentionDays(value);
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined retentionDays.
     */
    public abstract static class ExcludeReferencesSCMHeadFilter extends SCMHeadFilter {

        private final long acceptableDateTimeThreshold;

        protected ExcludeReferencesSCMHeadFilter(int retentionDays) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
        }

        public long getAcceptableDateTimeThreshold() {
            return acceptableDateTimeThreshold;
        }

        @Override
        public abstract boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException;
    }
}
