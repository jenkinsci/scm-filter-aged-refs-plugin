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
import static org.hamcrest.MatcherAssert.assertThat;

public class GitHubAgedRefsTraitTest {

    @ClassRule
    public static final JenkinsRule j = new JenkinsRule();

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
    public void plugin_defaults() {
        GitHubSCMSource instance = (GitHubSCMSource) load();
        assertThat(instance.getTraits(),
                contains(
                        Matchers.allOf(
                                instanceOf(GitHubAgedRefsTrait.class),
                                hasProperty("branchRetentionDays", is(0)),
                                hasProperty("prRetentionDays", is(0)),
                                hasProperty("tagRetentionDays", is(0)),
                                hasProperty("branchExcludeFilter", is(""))
                        )
                )
        );
    }

    @Test
    public void plugin_enabled() {
        GitHubSCMSource instance = (GitHubSCMSource) load();
        assertThat(instance.getTraits(),
                contains(
                        Matchers.allOf(
                                instanceOf(GitHubAgedRefsTrait.class),
                                hasProperty("branchRetentionDays", is(30)),
                                hasProperty("prRetentionDays", is(40)),
                                hasProperty("tagRetentionDays", is(50)),
                                hasProperty("branchExcludeFilter", is("main hotfix-*"))
                        )
                )
        );
    }
}
