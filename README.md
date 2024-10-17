# Branch source Aged Refs

[![GitHub release](https://img.shields.io/github/v/release/jenkinsci/scm-filter-aged-refs-plugin)](https://github.com/jenkinsci/scm-filter-aged-refs-plugin/releases/latest)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fscm-filter-aged-refs-plugin%2Fmain)](https://ci.jenkins.io/job/Plugins/job/scm-filter-aged-refs-plugin/job/main/)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/scm-filter-aged-refs-plugin)](https://github.com/jenkinsci/scm-filter-aged-refs-plugin/blob/main/LICENSE)
[![Maintenance](https://img.shields.io/maintenance/yes/2024)]()

This is a collection of extensions for several branch source Jenkins plugins.

It provides filters for

- [Bitbucket]: ![BitBucket Plugin installs](https://img.shields.io/jenkins/plugin/i/bitbucket-scm-filter-aged-refs?color=blue) Filtering references (branches, tags and pull requests)
- [GitHub]: ![GitHub Plugin installs](https://img.shields.io/jenkins/plugin/i/github-scm-filter-aged-refs?color=blue) Filtering references (branches, tags and pull requests)

This filter will ignore references (branches, pull requests and/or tags) where its last
commit creation date is older than the defined threshold (in days). Ignored
references won't be added as sources or will be disabled and tagged for
deletion on the next full repository scan.

[Bitbucket]: https://plugins.jenkins.io/cloudbees-bitbucket-branch-source/
[GitHub]: https://plugins.jenkins.io/github-branch-source/

## Usage

When defining a new job (or Organization folder), include an additional
behaviour (placed under the additional separator). 

![Dropdown Screenshot](.github/images/dropdown.png)

This behaviour is configurable, being mandatory to specify the
acceptable threshold (positive days) for each reference.
It can be configured to exclude a reference type from this filter, for example, excluding the branches so the acceptable threshold is only applied to pull requests and tags.

![Config Screenshot](.github/images/config.png)

In case of an invalid positive threshold, the form won't validate:

![Error Screenshot](.github/images/config-invalid.png)
