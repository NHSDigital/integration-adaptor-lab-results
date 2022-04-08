# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.6]
### Added
n/a

### Changed
- Hotfix for Spring vulnerability CVE-2022-22965

### Removed
n/a
## [0.0.5] - 2021-07-01
### Added
n/a

### Changed
- Fixed issue where processing Screening partial acceptance with NHSACK EDIFACT data logged an "Array index out of range" error (See https://github.com/nhsconnect/integration-adaptor-lab-results/issues/158)

### Removed
n/a

### Known Issues

Issues with no direct upgrade or patch:
- Improper Certificate Validation [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268] in io.netty:netty-handler@4.1.60.Final
introduced by org.apache.qpid:qpid-jms-client@0.56.0 > io.netty:netty-handler@4.1.60.Final and 1 other path(s)
- Denial of Service (DoS) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1078499] in net.minidev:json-smart@2.3
introduced by org.springframework.boot:spring-boot-starter-test@2.4.4 > com.jayway.jsonpath:json-path@2.4.0 > net.minidev:json-smart@2.3
- Denial of Service (DoS) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1298655] in net.minidev:json-smart@2.3
introduced by org.springframework.boot:spring-boot-starter-test@2.4.4 > com.jayway.jsonpath:json-path@2.4.0 > net.minidev:json-smart@2.3
- Comparison Using Wrong Factors [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448] in org.bouncycastle:bcprov-jdk15on@1.66
introduced by com.heroku.sdk:env-keystore@1.1.6 > org.bouncycastle:bcpkix-jdk15on@1.66 > org.bouncycastle:bcprov-jdk15on@1.66
- Improper Input Validation [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGGLASSFISH-1297098] in org.glassfish:jakarta.el@3.0.3
introduced by org.springframework.boot:spring-boot-starter-web@2.4.4 > org.springframework.boot:spring-boot-starter-tomcat@2.4.4 > org.glassfish:jakarta.el@3.0.3

## [0.0.4] - 2021-06-18
### Added
n/a

### Changed
Bugs fixed:
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/115
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/117
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/120
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/121
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/122
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/123
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/129
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/130
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/131
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/133
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/135
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/143
- https://github.com/nhsconnect/integration-adaptor-lab-results/issues/152

### Removed
n/a

### Known Issues

Issues with no direct upgrade or patch:
- Denial of Service (DoS) (new) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1298655] in net.minidev:json-smart@2.3.1 introduced by net.minidev:json-smart@2.3.1
- Denial of Service (DoS) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1078499] in net.minidev:json-smart@2.3.1 introduced by net.minidev:json-smart@2.3.1
- Comparison Using Wrong Factors [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448] in org.bouncycastle:bcprov-jdk15on@1.66 introduced by org.bouncycastle:bcpkix-jdk15on@1.66 > org bouncycastle:bcprov-jdk15on@1.66
- Improper Certificate Validation [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268] in io.netty:netty-handler@4.1.65.Final introduced by io.netty:netty-handler@4.1.65.Final
- Improper Input Validation [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGGLASSFISH-1297098] in org.glassfish:jakarta.el@3.0.3 introduced by org.springframework.boot:spring-boot-starter-tomcat@2.4.6 > org.glassfish:jakarta.el@3.0.3

## [0.0.3] - 2021-03-26
### Added
- NHS002 translations fully implemented
- Documentation updated
- Fixed https://github.com/nhsconnect/integration-adaptor-lab-results/issues/106

### Changed
n/a

### Removed
n/a

### Known Issues

Issues with no direct upgrade or patch:
- Comparison Using Wrong Factors [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448] in org.bouncycastle:bcprov-jdk15on@1.66 introduced by org.bouncycastle:bcpkix-jdk15on@1.66 > org.bouncycastle:bcprov-jdk15on@1.66
- Denial of Service (DoS) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1078499] in net.minidev:json-smart@2.3 introduced by net.minidev:json-smart@2.3
- Improper Certificate Validation [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268] in io.netty:netty-handler@4.1.60.Final introduced by io.netty:netty-handler@4.1.60.Final

## [0.0.2] - 2021-03-19
### Added
- NHS003 and NHS004 translations fully implemented
- NHSACK created and sent back to EDIFACT originator if requested
- `MessageType` and `Checksum` headers set on `Outbound GP Queue`
- Documentation updated

### Changed
n/a

### Removed
n/a

### Known Issues

Issues with no direct upgrade or patch:
- Comparison Using Wrong Factors [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448] in org.bouncycastle:bcprov-jdk15on@1.66 introduced by org.bouncycastle:bcpkix-jdk15on@1.66 > org.bouncycastle:bcprov-jdk15on@1.66
- Denial of Service (DoS) [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-NETMINIDEV-1078499] in net.minidev:json-smart@2.3 introduced by net.minidev:json-smart@2.3
- Improper Certificate Validation [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268] in io.netty:netty-handler@4.1.60.Final introduced by io.netty:netty-handler@4.1.60.Final

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
