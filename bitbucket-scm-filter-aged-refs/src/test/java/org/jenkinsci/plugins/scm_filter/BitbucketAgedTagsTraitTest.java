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
public class BitbucketAgedTagsTraitTest {
    private BitbucketSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (BitbucketSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData(JenkinsRule ignoredRule) throws IOException {
        BitbucketSCMSource instance = load("exclude_tags_thirty_days.xml");
        assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(BitbucketAgedTagsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30);
    }
}
