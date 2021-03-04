# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.1] - 2021-02-22
### Added
- First release of Lab Results Adaptor
- NHSACK is not produced
- Limited set of resources produced - Requester (FHIR Practitioner resource)

### Changed
n/a

### Removed
n/a

### Known Issues
- Information Disclosure [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415] in com.google.guava:guava@29.0-jre introduced by com.google.guava:guava@29.0-jre
- Information Disclosure (new) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1070799] in io.netty:netty-codec-http@4.1.55.Final introduced by io.netty:netty-codec-http@4.1.55.Final
- Comparison Using Wrong Factors [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448] in org.bouncycastle:bcprov-jdk15on@1.66 introduced by org.bouncycastle:bcpkix-jdk15on@1.66 > org.bouncycastle:bcprov-jdk15on@1.66
- Improper Certificate Validation [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268] in io.netty:netty-handler@4.1.55.Final introduced by io.netty:netty-handler@4.1.55.Final
