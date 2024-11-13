package org.jenkinsci.plugins.scm_filter.enums;

public enum RefType {
    BRANCH("Branch"),
    PULL_REQUEST("Pull Request"),
    TAG("Tag");

    private final String name;

    RefType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
