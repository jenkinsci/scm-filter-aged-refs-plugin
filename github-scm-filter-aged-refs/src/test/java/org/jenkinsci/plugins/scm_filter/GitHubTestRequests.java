package org.jenkinsci.plugins.scm_filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

final class GitHubTestRequests {

    private GitHubTestRequests() {}

    static GitHubSCMSourceRequest withOldBranch(String branchName, String defaultBranch) throws IOException {
        GHRepository repository = mock(GHRepository.class);
        when(repository.getDefaultBranch()).thenReturn(defaultBranch);
        GHCommit commit = mock(GHCommit.class);
        when(commit.getCommitDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60)));
        when(repository.getCommit("0123456789abcdef")).thenReturn(commit);
        GHBranch branch = mock(GHBranch.class);
        when(branch.getName()).thenReturn(branchName);
        when(branch.getSHA1()).thenReturn("0123456789abcdef");
        when(branch.getOwner()).thenReturn(repository);
        GitHubSCMSourceRequest request = mock(GitHubSCMSourceRequest.class);
        when(request.getRepository()).thenReturn(repository);
        when(request.getBranches()).thenReturn(List.of(branch));
        return request;
    }
}
