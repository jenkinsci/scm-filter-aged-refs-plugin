package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;

public abstract class AgedRefsTrait extends SCMSourceTrait {

    final int branchRetentionDays;
    final int prRetentionDays;
    final int tagRetentionDays;
    final String branchExcludeFilter; 

    /**
     * Constructor for stapler.
     *
     * @param branchRetentionDays retention period in days for branches
     * @param prRetentionDays retention period in days for pull requests
     * @param tagRetentionDays retention period in days for tags
     * @param branchExcludeFilter space-separated list of branch name patterns to ignore. For example: release main hotfix-*
     */
    protected AgedRefsTrait(String branchRetentionDays, String prRetentionDays, String tagRetentionDays, String branchExcludeFilter) {
        this.branchRetentionDays = Integer.parseInt(branchRetentionDays);
        this.prRetentionDays = Integer.parseInt(prRetentionDays);
        this.tagRetentionDays = Integer.parseInt(tagRetentionDays);
        this.branchExcludeFilter = branchExcludeFilter;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getBranchRetentionDays() {
        return this.branchRetentionDays;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getPrRetentionDays() {
        return this.prRetentionDays;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getTagRetentionDays() {
        return this.tagRetentionDays;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public String getBranchExcludeFilter() {
        return this.branchExcludeFilter;
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
        public FormValidation doCheckRetentionDays(@QueryParameter String branchRetentionDays, @QueryParameter String prRetentionDays, @QueryParameter String tagRetentionDays) {
            FormValidation formValidation = FormValidation.ok();

            try {
                if (branchRetentionDays == null || value.branchRetentionDays()) {
                    formValidation = FormValidation.error("Branch retention days are not a number");
                } else {
                    int val = Integer.parseInt(branchRetentionDays);
                    if (val < 0) {
                        formValidation = FormValidation.error("Branch retention days are not a positive number");
                    }
                }
            } catch (NumberFormatException e) {
                formValidation = FormValidation.error("Branch retention days are not a number");
            }

            try {
                if (prRetentionDays == null || value.prRetentionDays()) {
                    formValidation = FormValidation.error("PR retention days are not a number");
                } else {
                    int val = Integer.parseInt(prRetentionDays);
                    if (val < 0) {
                        formValidation = FormValidation.error("PR retention days are not a positive number");
                    }
                }
            } catch (NumberFormatException e) {
                formValidation = FormValidation.error("PR retention days are not a number");
            }

            try {
                if (tagRetentionDays == null || value.tagRetentionDays()) {
                    formValidation = FormValidation.error("Tag retention days are not a number");
                } else {
                    int val = Integer.parseInt(tagRetentionDays);
                    if (val < 0) {
                        formValidation = FormValidation.error("Tag retention days are not a positive number");
                    }
                }
            } catch (NumberFormatException e) {
                formValidation = FormValidation.error("Tag retention days are not a number");
            }

            return formValidation;
        }
    }

    /**
     * Filter that excludes references (branches, pull requests, tags) according to their last commit modification date and the defined branchRetentionDays.
     */
    public abstract static class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {

        private final long acceptableBranchDateTimeThreshold;
        private final long acceptablePRDateTimeThreshold;
        private final long acceptableTagDateTimeThreshold;
        private final String branchExcludePattern;

        /**
         * Returns the pattern corresponding to the branches containing wildcards.
         *
         * @param branches the names of branches to create a pattern for
         * @return pattern corresponding to the branches containing wildcards
         */
        private String getPattern(String branches) {
            if (branches.equals("")) {
                return "";
            }

            StringBuilder quotedBranches = new StringBuilder();
            for (String wildcard : branches.split(" ")) {
                StringBuilder quotedBranch = new StringBuilder();
                for (String branch : wildcard.split("(?=[*])|(?<=[*])")) {
                    if (branch.equals("*")) {
                        quotedBranch.append(".*");
                    } else if (!branch.isEmpty()) {
                        quotedBranch.append(Pattern.quote(branch));
                    }
                }
                if (quotedBranches.length() > 0) {
                    quotedBranches.append("|");
                }
                quotedBranches.append(quotedBranch);
            }
            return quotedBranches.toString();
        }

        protected ExcludeBranchesSCMHeadFilter(int branchRetentionDays, int prRetentionDays, int tagRetentionDays, String branchExcludeFilter) {
            this.branchExcludePattern = this.getPattern(branchExcludeFilter);

            long now = System.currentTimeMillis();

            if (branchRetentionDays > 0) {
                this.acceptableBranchDateTimeThreshold = now - (24L * 60 * 60 * 1000 * branchRetentionDays);
            } else {
                this.acceptableBranchDateTimeThreshold = 0;
            }

            if (prRetentionDays > 0 ) {
                this.acceptablePRDateTimeThreshold = now - (24L * 60 * 60 * 1000 * prRetentionDays);
            } else {
                this.acceptablePRDateTimeThreshold = 0;
            }

            if (tagRetentionDays > 0) {
                this.acceptableTagDateTimeThreshold = now - (24L * 60 * 60 * 1000 * tagRetentionDays);
            } else {
                this.acceptableTagDateTimeThreshold = 0;
            }
        }

        public long getAcceptableBranchDateTimeThreshold() {
            return acceptableBranchDateTimeThreshold;
        }

        public long getAcceptablePRDateTimeThreshold() {
            return acceptablePRDateTimeThreshold;
        }

        public long getAcceptableTagDateTimeThreshold() {
            return acceptableTagDateTimeThreshold;
        }

        public String getBranchExcludePattern() {
            return branchExcludePattern;
        }

        @Override
        public abstract boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException;
    }
}
