package org.jenkinsci.plugins.scm_filter.utils;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketTagSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BitbucketFilterRefUtils {

    private static final Logger LOGGER = Logger.getLogger(BitbucketFilterRefUtils.class.getName());

    private BitbucketFilterRefUtils() {
        // Helper class
    }

    public static boolean isBranchExcluded(
            @NonNull BitbucketSCMSourceRequest scmSourceRequest,
            @NonNull BranchSCMHead scmHead,
            long acceptableDateTimeThreshold) {
        try {
            for (BitbucketBranch branch : scmSourceRequest.getBranches()) {
                long branchTS = branch.getDateMillis();
                if (branch.getName().equals(scmHead.getName())) {
                    return branchTS < acceptableDateTimeThreshold;
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Could not lookup branch " + scmHead.getName());
        }
        return false;
    }

    public static boolean isPullRequestExcluded(
            @NonNull BitbucketSCMSourceRequest scmSourceRequest,
            @NonNull PullRequestSCMHead scmHead,
            long acceptableDateTimeThreshold) {
        try {
            for (BitbucketPullRequest pull : scmSourceRequest.getPullRequests()) {
                if (pull.getSource().getBranch().getName().equals(scmHead.getName())) {
                    long pullTS =
                            pull.getSource().getCommit().getCommitterDate().getTime();
                    return pullTS < acceptableDateTimeThreshold;
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Could not lookup pull request " + scmHead.getName());
        }
        return false;
    }

    public static boolean isTagExcluded(@NonNull BitbucketTagSCMHead scmHead, long acceptableDateTimeThreshold) {
        long tagTS = scmHead.getTimestamp();
        return tagTS < acceptableDateTimeThreshold;
    }
}
