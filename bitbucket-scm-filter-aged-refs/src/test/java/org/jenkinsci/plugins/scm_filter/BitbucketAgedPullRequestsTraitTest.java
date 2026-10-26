package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.PullRequestBranchType;
import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class BitbucketAgedPullRequestsTraitTest {
    private BitbucketSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (BitbucketSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData(JenkinsRule ignoredRule) throws IOException {
        BitbucketSCMSource instance = load("exclude_pull_requests_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(BitbucketAgedPullRequestsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefPatterns", "");
    }

    @Test
    void retainedPullRequestBypassesAgeLookup(JenkinsRule ignoredRule) throws IOException, InterruptedException {
        BitbucketAgedPullRequestsTrait.ExcludeOldPullRequestsSCMHeadFilter filter =
                new BitbucketAgedPullRequestsTrait.ExcludeOldPullRequestsSCMHeadFilter(30, "PR-123");
        PullRequestSCMHead head = new PullRequestSCMHead(
                "PR-123",
                "owner",
                "repository",
                "feature",
                PullRequestBranchType.BRANCH,
                "123",
                "Title",
                new BranchSCMHead("main"),
                SCMHeadOrigin.DEFAULT,
                ChangeRequestCheckoutStrategy.HEAD);

        assertThat(filter.isExcluded(null, head)).isFalse();
    }
}
