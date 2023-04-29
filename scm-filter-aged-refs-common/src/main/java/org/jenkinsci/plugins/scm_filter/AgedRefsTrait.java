package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import java.io.IOException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;

public abstract class AgedRefsTrait extends SCMSourceTrait {

    final int retentionDays;

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
        public FormValidation doCheckRetentionDays(@QueryParameter String value) {
            FormValidation formValidation = FormValidation.ok();

            try {
                if (value == null || value.isBlank()) {
                    formValidation = FormValidation.error("Not a number");
                } else {
                    int val = Integer.parseInt(value);
                    if (val < 1) {
                        formValidation = FormValidation.error("Not a positive number");
                    }
                }
            } catch (NumberFormatException e) {
                formValidation = FormValidation.error("Not a number");
            }

            return formValidation;
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined retentionDays.
     */
    public abstract static class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {

        private final long acceptableDateTimeThreshold;

        protected ExcludeBranchesSCMHeadFilter(int retentionDays) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
        }

        public long getAcceptableDateTimeThreshold() {
            return acceptableDateTimeThreshold;
        }

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException {
            if (scmHead instanceof TagSCMHead) {
                long tagTS = ((TagSCMHead) scmHead).getTimestamp();
                return tagTS < getAcceptableDateTimeThreshold();
            }
            return false;
        }
    }
}
