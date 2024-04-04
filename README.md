# Cap-stone Server

## About
An ecommerce application still in development.

## Getting started
#### Pre-requisite
1. docker

```
To run application
./mvnw clean spring-boot:test-run
or
mvn clean spring-boot:test-run

To run all test
mvn clean test
```

### Technologies
* Java 21.
* Spring Boot 3.2.x
  * Spring Starter Web.
  * Spring Starter Validation.
  * Spring Data JPA.
  * Spring Oauth2 Resource Server (manual authentication and authorization using jwt).
  * Spring Session.
* MySQL.
* Flyway.
* Paystack.
* Awaitility.
* AWS Java SDK v2 (SESv2, S3Client, S3Presigner and SecretsManager Client).
* Unit testing, Data Access Layer and Integration Testing using JUnit, Mockito, Hamcrest and Test Containers.

### Schema
Link for the most up-to-date schema.
[Link](https://dbdiagram.io/d/6483c4d5722eb77494b791a1)

### Development links
[Storefront](https://server.emmanueluluabuike.com/)
&
[Admin front](https://server.emmanueluluabuike.com/admin)

### Development information
* [naming index convention](https://www.quora.com/What-naming-convention-do-you-use-for-SQL-indexes)
* [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)