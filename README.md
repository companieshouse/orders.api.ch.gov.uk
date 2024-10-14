# Companies House Orders API
## orders.api.ch.gov.uk
API handling CRUD operations on CH Ordering Service

### Requirements
* [Java 21][1]
* [Maven][2]
* [Git][3]

### Getting Started
1. Run `make` to build
2. Run `./start.sh` to run

### Environment Variables
| Name            | Description                                  | Mandatory | Location |
|-----------------|----------------------------------------------|-----------|----------|
| ORDERS_API_PORT | Port this application runs on when deployed. | ✓         | start.sh |

### Endpoints
| Path                                       | Method | Description                                                         |
|--------------------------------------------|--------|---------------------------------------------------------------------|
| *`/basket`*                                | PATCH  | Adds delivery details to `basket`.                                  |
| *`/basket/items`*                          | POST   | Adds requested `item` to `basket`.                                  |
| *`/basket/checkouts`*                      | POST   | Processes `basket` checkout. Creates a `checkout` resource.         |
| *`/basket/checkouts/{checkoutId}/payment`* | GET    | Returns `paymentDetails` resource for a valid `checkoutId`.         |
| *`/basket/checkouts/{checkoutId}/payment`* | PATCH  | Updates `checkout` resource with payment status.                    |
| *`/orders/{orderId}`*                      | GET    | Returns `OrderData` resource for a valid `orderId`.                 |
| *`/orders/{orderId}/reprocess`*            | POST   | Triggers the re-processing of the order.                            |
| *`/healthcheck`*                           | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

[1]: https://www.oracle.com/java/technologies/downloads/#java21
[2]: https://maven.apache.org/download.cgi
[3]: https://git-scm.com/downloads

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        | order-service                                     | ECS cluster (stack) the service belongs to
**Load balancer**      | {env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private                                 | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/orders.api.ch.gov.uk) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/orders.api.ch.gov.uk)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)