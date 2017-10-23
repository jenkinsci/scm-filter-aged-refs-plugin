package org.jenkinsci.plugins.github_source_aged;

import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AgedRefsStrategyTraitTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    @Rule
    public TestName currentTestName = new TestName();

    private SCMSource load() {
        return load(currentTestName.getMethodName());
    }

    private SCMSource load(String dataSet) {
        return (GitHubSCMSource) Jenkins.XSTREAM2.fromXML(
                getClass().getResource(getClass().getSimpleName() + "/" + dataSet + ".xml"));
    }

    @Test
    public void exclude_thirty_days() throws Exception {
        GitHubSCMSource instance = (GitHubSCMSource) load();
        assertThat(instance.getTraits(),
                containsInAnyOrder(
                        Matchers.<SCMSourceTrait>allOf(
                                instanceOf(AgedRefsStrategyTrait.class),
                                hasProperty("retentionDays", is(30))
                        )
                )
        );
    }
}