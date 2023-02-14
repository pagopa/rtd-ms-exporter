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