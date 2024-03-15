package org.jenkinsci.plugins.scm_filter;

import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource;
import java.io.IOException;
import java.io.InputStream;
import jenkins.model.Jenkins;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class GitLabAgedRefsTraitTest {

    private GitLabSCMSource load(String file) throws IOException {
        try (InputStream res = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + file)) {
            return (GitLabSCMSource) Jenkins.XSTREAM2.fromXML(res);
        }
    }

    @Test
    void restoreData() throws IOException {
        GitLabSCMSource instance = load("exclude_thirty_days.xml");
        Assertions.assertThat(instance.getTraits())
                .singleElement()
                .isInstanceOf(GitLabAgedRefsTrait.class)
                .hasFieldOrPropertyWithValue("retentionDays", 30);
    }
}
