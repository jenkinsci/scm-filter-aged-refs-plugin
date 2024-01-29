package org.jenkinsci.plugins.scm_filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

public class BitbucketAgedRefsTraitTest {

    @ClassRule
    public static final JenkinsRule j = new JenkinsRule();

    @Rule
    public TestName currentTestName = new TestName();

    private SCMSource load() {
        return load(currentTestName.getMethodName());
    }

    private SCMSource load(String dataSet) {
        return (BitbucketSCMSource)
                Jenkins.XSTREAM2.fromXML(getClass().getResource(getClass().getSimpleName() + "/" + dataSet + ".xml"));
    }

    @Test
    public void plugin_defaults() {
        BitbucketSCMSource instance = (BitbucketSCMSource) load();
        assertThat(
                instance.getTraits(),
                contains(Matchers.allOf(
                        instanceOf(BitbucketAgedRefsTrait.class),
                        hasProperty("branchRetentionDays", is(0)),
                        hasProperty("prRetentionDays", is(0)),
                        hasProperty("tagRetentionDays", is(0)),
                        hasProperty("branchExcludeFilter", is("")))));
    }

    @Test
    public void plugin_enabled() {
        BitbucketSCMSource instance = (BitbucketSCMSource) load();
        assertThat(
                instance.getTraits(),
                contains(Matchers.allOf(
                        instanceOf(BitbucketAgedRefsTrait.class),
                        hasProperty("branchRetentionDays", is(30)),
                        hasProperty("prRetentionDays", is(40)),
                        hasProperty("tagRetentionDays", is(50)),
                        hasProperty("branchExcludeFilter", is("main hotfix-*")))));
    }
}
