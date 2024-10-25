package org.jenkinsci.plugins.scm_filter.utils;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketTagSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import edu.umd.cs.findbugs.annotations.NonNull;

public final class FilterRefUtils {
    public static boolean isBranchExcluded(
            @NonNull BitbucketSCMSourceRequest scmSourceRequest,
            @NonNull BranchSCMHead scmHead,
            long acceptableDateTimeThreshold) {
        Iterable<BitbucketBranch> branches = scmSourceRequest.getBranches();
        for (BitbucketBranch branch : branches) {
            long branchTS = branch.getDateMillis();
            if (branch.getName().equals(scmHead.getName())) {
                return branchTS < acceptableDateTimeThreshold;
            }
        }
        return false;
    }

    public static boolean isPullRequestExcluded(
            @NonNull BitbucketSCMSourceRequest scmSourceRequest,
            @NonNull PullRequestSCMHead scmHead,
            long acceptableDateTimeThreshold) {
        Iterable<BitbucketPullRequest> pulls = scmSourceRequest.getPullRequests();
        for (BitbucketPullRequest pull : pulls) {
            if (pull.getSource().getBranch().getName().equals(scmHead.getName())) {
                long pullTS = pull.getSource().getCommit().getDateMillis();
                return pullTS < acceptableDateTimeThreshold;
            }
        }
        return false;
    }

    public static boolean isTagExcluded(@NonNull BitbucketTagSCMHead scmHead, long acceptableDateTimeThreshold) {
        long tagTS = scmHead.getTimestamp();
        return tagTS < acceptableDateTimeThreshold;
    }
}
