package org.jenkinsci.plugins.scm_filter.utils;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.GitHubTagSCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.PagedIterator;

public class FilterRefUtils {
    public static boolean isBranchExcluded(
            GitHubSCMSourceRequest scmSourceRequest, BranchSCMHead scmHead, long acceptableDateTimeThreshold)
            throws IOException {
        Iterable<GHBranch> branches = scmSourceRequest.getBranches();
        for (GHBranch branch : branches) {
            long branchTS = branch.getOwner()
                    .getCommit(branch.getSHA1())
                    .getCommitDate()
                    .getTime();
            if (branch.getName().equals(scmHead.getName())) {
                return branchTS < acceptableDateTimeThreshold;
            }
        }
        return false;
    }

    public static boolean isPullRequestExcluded(
            GitHubSCMSourceRequest scmSourceRequest, PullRequestSCMHead scmHead, long acceptableDateTimeThreshold) {
        int pullNr = scmHead.getNumber();
        Iterable<GHPullRequest> pulls = scmSourceRequest.getPullRequests();
        Optional<GHPullRequest> pull = StreamSupport.stream(pulls.spliterator(), false)
                .filter(p -> pullNr == p.getNumber())
                .findAny();
        if (pull.isPresent()) {
            PagedIterator<GHPullRequestCommitDetail> iterator =
                    pull.get().listCommits().iterator();
            long latestPullTS = 0;
            while (iterator.hasNext()) {
                long commitTS = iterator.next()
                        .getCommit()
                        .getCommitter()
                        .getDate()
                        .getTime();
                if (commitTS > latestPullTS) latestPullTS = commitTS;
            }
            // Did we see at least one commit?
            if (latestPullTS > 0) return latestPullTS < acceptableDateTimeThreshold;
        }
        return false;
    }

    public static boolean isTagExcluded(GitHubTagSCMHead scmHead, long acceptableDateTimeThreshold) {
        long tagTS = scmHead.getTimestamp();
        return tagTS < acceptableDateTimeThreshold;
    }
}
