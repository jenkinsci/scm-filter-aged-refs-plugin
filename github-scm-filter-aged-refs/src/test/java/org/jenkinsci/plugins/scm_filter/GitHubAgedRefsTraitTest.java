package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class GitHubAgedRefsTraitTest {

    private GitHubSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreDefaultData(JenkinsRule ignoredRule) throws IOException {
        GitHubSCMSource instance = load("plugin_defaults.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("branchRetentionDays", 0)
                .hasFieldOrPropertyWithValue("prRetentionDays", 0)
                .hasFieldOrPropertyWithValue("tagRetentionDays", 0)
                .hasFieldOrPropertyWithValue("branchExcludeFilter", "");
    }

    @Test
    void restoreExistingData(JenkinsRule ignoredRule) throws IOException {
        GitHubSCMSource instance = load("plugin_enabled.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitHubAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("branchRetentionDays", 30)
                .hasFieldOrPropertyWithValue("prRetentionDays", 40)
                .hasFieldOrPropertyWithValue("tagRetentionDays", 50)
                .hasFieldOrPropertyWithValue("branchExcludeFilter", "main hotfix-*");
    }
}
