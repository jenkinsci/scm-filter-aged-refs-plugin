package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.jenkinsci.plugins.scm_filter.utils.FormValidationUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public abstract class AgedRefsTrait extends SCMSourceTrait {

    final int retentionDays;

    private String retainedRefsRegex;

    private transient Pattern retainedRefsPattern;

    private String retainedRefsExcludeRegex;

    private transient Pattern retainedRefsExcludePattern;

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

    @SuppressWarnings("unused") // used by Jelly EL
    public String getRetainedRefsRegex() {
        return retainedRefsRegex;
    }

    @DataBoundSetter
    public void setRetainedRefsRegex(String retainedRefsRegex) {
        String regex = Util.fixEmptyAndTrim(retainedRefsRegex);
        this.retainedRefsPattern = regex == null ? null : Pattern.compile(regex);
        this.retainedRefsRegex = regex;
    }

    protected Pattern getRetainedRefsPattern() {
        if (retainedRefsPattern == null && retainedRefsRegex != null) {
            retainedRefsPattern = Pattern.compile(retainedRefsRegex);
        }
        return retainedRefsPattern;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public String getRetainedRefsExcludeRegex() {
        return retainedRefsExcludeRegex;
    }

    @DataBoundSetter
    public void setRetainedRefsExcludeRegex(String retainedRefsExcludeRegex) {
        String regex = Util.fixEmptyAndTrim(retainedRefsExcludeRegex);
        this.retainedRefsExcludePattern = regex == null ? null : Pattern.compile(regex);
        this.retainedRefsExcludeRegex = regex;
    }

    protected Pattern getRetainedRefsExcludePattern() {
        if (retainedRefsExcludePattern == null && retainedRefsExcludeRegex != null) {
            retainedRefsExcludePattern = Pattern.compile(retainedRefsExcludeRegex);
        }
        return retainedRefsExcludePattern;
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

        @Restricted(NoExternalUse.class)
        @POST
        public FormValidation doCheckRetainedRefsRegex(@QueryParameter String value) {
            return FormValidationUtils.checkRegex(value);
        }

        @Restricted(NoExternalUse.class)
        @POST
        public FormValidation doCheckRetainedRefsExcludeRegex(@QueryParameter String value) {
            return FormValidationUtils.checkRegex(value);
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined retentionDays.
     */
    public abstract static class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {

        private final long acceptableDateTimeThreshold;

        private final Pattern retainedRefs;

        private final Pattern retainedRefsExcludes;

        protected ExcludeBranchesSCMHeadFilter(int retentionDays) {
            this(retentionDays, null, null);
        }

        protected ExcludeBranchesSCMHeadFilter(int retentionDays, Pattern retainedRefs, Pattern retainedRefsExcludes) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
            this.retainedRefs = retainedRefs;
            this.retainedRefsExcludes = retainedRefsExcludes;
        }

        protected boolean isRetained(@NonNull SCMHead scmHead) {
            return retainedRefs != null
                    && retainedRefs.matcher(scmHead.getName()).matches()
                    && !isRetentionExcluded(scmHead);
        }

        protected boolean isRetentionExcluded(@NonNull SCMHead scmHead) {
            return retainedRefsExcludes != null
                    && retainedRefsExcludes.matcher(scmHead.getName()).matches();
        }

        public long getAcceptableDateTimeThreshold() {
            return acceptableDateTimeThreshold;
        }

        @Override
        public abstract boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead)
                throws IOException, InterruptedException;
    }
}
