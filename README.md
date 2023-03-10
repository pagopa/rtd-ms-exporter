# RTD Exporter
![version](https://img.shields.io/github/v/release/pagopa/rtd-ms-exporter)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pagopa_rtd-ms-exporter&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pagopa_rtd-ms-exporter)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=pagopa_rtd-ms-exporter&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=pagopa_rtd-ms-exporter)

This microservice allows to export ready-to-acquirer cards file to filter incoming transactions.

## Environment Variables
| Variable name         | Default     | Description                                 |
|-----------------------|-------------|---------------------------------------------|
| CRON_EXPRESSION_BATCH | 0 * * * * * | Expression to manage batch service schedule |
|                       |             |                                             |
|                       |             |                        


## GraalVM general setup
Download GraalVM sdk for Java17 and set the following environment variables:
- GRAALVM_HOME: path/to/graalvm/folder

Add export directive to .bash_profile and .zshrc

## Generate reflection tracing metadata (when needed)
Spring AOT provide a great support for analyze spring boot application and generate a tracing metadata files.
Sometimes can be a lack of that tracing especially when using external library, so GraalVM and spring boot suggests
to use the graalvm agent to trace these metadata.

More info refers to: https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced
and https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#agent-support

So these step must be followed.
1. Execute tests or jar by attaching graalvm agent
2. Copy generate metadata to "src/main/resource/META-INF/native-image"
3. Now, compile or run native

The build.gradle attached to this project already configure agent by using gradle plugin.
You can choose to which task attach the agent. 

The script provide a task "testWithAgent" which run test by attaching graalvm agent which collect metadata.
After that a "metadataCopy" task must be run to copy to proper folder.
