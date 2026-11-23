package org.jenkinsci.plugins.scm_filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceContext;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketTagSCMHead;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;
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
                .hasFieldOrPropertyWithValue("retentionDays", 30)
                .hasFieldOrPropertyWithValue("retainedRefsRegex", null)
                .hasFieldOrPropertyWithValue("retainedRefsExcludeRegex", null);
    }

    @Test
    void excludeRegexOverridesKeepRegex() throws Exception {
        BitbucketAgedTagsTrait trait = new BitbucketAgedTagsTrait("30");
        trait.setRetainedRefsRegex("v.*");
        trait.setRetainedRefsExcludeRegex("v0\\..*");
        long old = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60);
        SCMHeadFilter filter = filter(trait);
        BitbucketSCMSourceRequest request = mock(BitbucketSCMSourceRequest.class);
        assertThat(filter.isExcluded(request, new BitbucketTagSCMHead("v1.0", old)))
                .isFalse();
        assertThat(filter.isExcluded(request, new BitbucketTagSCMHead("v0.9", old)))
                .isTrue();
    }

    @Test
    void regexKeepsOnlyMatchingOldTags() throws Exception {
        BitbucketAgedTagsTrait trait = new BitbucketAgedTagsTrait("30");
        trait.setRetainedRefsRegex("v1\\..*");
        long old = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60);
        SCMHeadFilter filter = filter(trait);
        BitbucketSCMSourceRequest request = mock(BitbucketSCMSourceRequest.class);
        assertThat(filter.isExcluded(request, new BitbucketTagSCMHead("v1.0", old)))
                .isFalse();
        assertThat(filter.isExcluded(request, new BitbucketTagSCMHead("v2.0", old)))
                .isTrue();
    }

    private static SCMHeadFilter filter(BitbucketAgedTagsTrait trait) {
        BitbucketSCMSourceContext context = new BitbucketSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(context);
        return context.filters().get(0);
    }
}
