package org.jenkinsci.plugins.scm_filter;

import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GitHubAgedRefsTraitTest {

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
    public void exclude_thirty_days() {
        GitHubSCMSource instance = (GitHubSCMSource) load();
        assertThat(instance.getTraits(),
                contains(
                        Matchers.allOf(
                                instanceOf(GitHubAgedRefsTrait.class),
                                hasProperty("retentionDays", is(30))
                        )
                )
        );
    }
}
