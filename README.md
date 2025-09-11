# Branch source Aged Refs

[![GitHub release](https://img.shields.io/github/v/release/jenkinsci/scm-filter-aged-refs-plugin)](https://github.com/jenkinsci/scm-filter-aged-refs-plugin/releases/latest)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fscm-filter-aged-refs-plugin%2Fmain)](https://ci.jenkins.io/job/Plugins/job/scm-filter-aged-refs-plugin/job/main/)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/scm-filter-aged-refs-plugin)](https://github.com/jenkinsci/scm-filter-aged-refs-plugin/blob/main/LICENSE)
![GitHub last commit](https://img.shields.io/github/last-commit/jenkinsci/scm-filter-aged-refs-plugin)

This is a collection of extensions for several branch source Jenkins plugins.

It provides filters for

- [Bitbucket]: ![BitBucket Plugin installs](https://img.shields.io/jenkins/plugin/i/bitbucket-scm-filter-aged-refs?color=blue)
  Filtering references (branches, tags and pull requests)
- [GitHub]: ![GitHub Plugin installs](https://img.shields.io/jenkins/plugin/i/github-scm-filter-aged-refs?color=blue)
  Filtering references (branches, tags and pull requests)

This filter will ignore references (branches, tags and/or pull requests) where its last
commit creation date is older than the defined threshold (in days). Ignored
references won't be added as sources or will be disabled and tagged for
deletion on the next full repository scan.

[Bitbucket]: https://plugins.jenkins.io/cloudbees-bitbucket-branch-source/

[GitHub]: https://plugins.jenkins.io/github-branch-source/

## Usage

There are four different behaviours available:

- **Filter by ref age**: Applies the days filter to branches, tags and pull requests.
- **Filter branches by age**: Applies the days filter only to branches.
- **Filter pull requests by age**: Applies the days filter only to pull requests.
- **Filter tags by age**: Applies the days filter only to tags.

![Dropdown Screenshot](.github/images/dropdown.png)

When defining a new job (or Organization folder), include any of the behaviours. They are placed under the additional
separator.

All behaviour are configurable, being mandatory to specify the
acceptable threshold (positive days) for each reference:

![Config Screenshot](.github/images/config.png)

In case of an invalid positive threshold, the form won't validate:

![Error Screenshot](.github/images/config-invalid.png)
