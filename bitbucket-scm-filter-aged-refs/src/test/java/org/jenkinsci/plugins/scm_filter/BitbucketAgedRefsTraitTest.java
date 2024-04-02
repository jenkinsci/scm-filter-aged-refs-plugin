package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BitbucketAgedRefsTraitTest {

    private BitbucketSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (BitbucketSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreDefaultData(JenkinsRule ignoredRule) throws IOException {
        BitbucketSCMSource instance = load("plugin_defaults.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(BitbucketAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("branchRetentionDays", 0)
                .hasFieldOrPropertyWithValue("prRetentionDays", 0)
                .hasFieldOrPropertyWithValue("tagRetentionDays", 0)
                .hasFieldOrPropertyWithValue("branchExcludeFilter", "");
    }

    @Test
    void restoreExistingData(JenkinsRule ignoredRule) throws IOException {
        BitbucketSCMSource instance = load("plugin_enabled.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(BitbucketAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("branchRetentionDays", 30)
                .hasFieldOrPropertyWithValue("prRetentionDays", 40)
                .hasFieldOrPropertyWithValue("tagRetentionDays", 50)
                .hasFieldOrPropertyWithValue("branchExcludeFilter", "main hotfix-*");
    }
}
