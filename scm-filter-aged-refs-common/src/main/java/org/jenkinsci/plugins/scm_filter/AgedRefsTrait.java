package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import java.io.IOException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.*;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public abstract class AgedRefsTrait extends SCMSourceTrait {
    final int retentionDays;
    final boolean filterBranches;
    final boolean filterPullRequests;
    final boolean filterTags;

    /**
     * Constructor for stapler.
     *
     * @param retentionDays retention period in days
     * @param filterBranches if filter should be applied to branches
     * @param filterPullRequests if filter should be applied to pull requests
     * @param filterTags if filter should be applied to tags
     */
    protected AgedRefsTrait(String retentionDays, String filterBranches, String filterPullRequests, String filterTags) {
        this.retentionDays = Integer.parseInt(retentionDays);
        this.filterBranches = Boolean.parseBoolean(filterBranches);
        this.filterPullRequests = Boolean.parseBoolean(filterPullRequests);
        this.filterTags = Boolean.parseBoolean(filterTags);
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getRetentionDays() {
        return this.retentionDays;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public boolean getFilterBranches() {
        return this.filterBranches;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public boolean getFilterPullRequests() {
        return this.filterPullRequests;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public boolean getFilterTags() {
        return this.filterTags;
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
        private final boolean filterBranches;
        private final boolean filterPullRequests;
        private final boolean filterTags;

        protected ExcludeBranchesSCMHeadFilter(
                int retentionDays, boolean filterBranches, boolean filterPullRequests, boolean filterTags) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
            this.filterBranches = filterBranches;
            this.filterPullRequests = filterPullRequests;
            this.filterTags = filterTags;
        }

        public long getAcceptableDateTimeThreshold() {
            return acceptableDateTimeThreshold;
        }

        public boolean shouldFilterBranches() {
            return filterBranches;
        }

        public boolean shouldFilterPullRequest() {
            return filterPullRequests;
        }

        public boolean shouldFilterTags() {
            return filterTags;
        }

        @Override
        public abstract boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException;
    }
}
