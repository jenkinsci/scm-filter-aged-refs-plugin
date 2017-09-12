package org.jenkinsci.plugins.branch_source_aged;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author witokondoria
 */
public abstract class AgedStrategyTrait extends SCMSourceTrait{

    public int retentionDays = 0;

    /**
     * Constructor for stapler.
     */
    public AgedStrategyTrait(String retentionDays){
        if (StringUtils.isBlank(retentionDays)) {
            this.retentionDays = Integer.MAX_VALUE;
        }
        this.retentionDays = Integer.parseInt(retentionDays);
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getRetentionDays() {
        return this.retentionDays;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract void decorateContext(SCMSourceContext<?, ?> context);

    /**
     * Our descriptor.
     */
    public abstract static class AgedRefsDescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Aged refs filtering strategy";
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicableToSCM(@NonNull SCMDescriptor<?> scm) {
            return scm instanceof GitSCM.DescriptorImpl;
        }
    }

    /**
     * Filter that excludes references (branches or pull requests) according to its last commit modification date and the defined retentionDays.
     */
    public abstract static class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {

        public long acceptableDateTimeThreshold;

        public ExcludeBranchesSCMHeadFilter(int retentionDays) {
            long now = System.currentTimeMillis();
            acceptableDateTimeThreshold = now - (24L * 60 * 60 * 1000 * retentionDays);
        }

        @Override
        abstract public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException;
    }
}
