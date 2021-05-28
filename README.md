# C.A.R. App

This is a fictitious app about some ride app (pretty much like Uber). 

My intention is just to have an app in my portfolio that demonstrates several practices and how to implement them using Kotlin. 

These practices are:


## Onion Architecture

This system is heavily based on the [Onion Architecture](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/). I believe my architecture doesn't follow the Onion strictly, because I modeled repositories after DDD, therefore repositories are a part of domain, not infrastructure. But the actual implementation of these repositories are handled by Spring Data, so in some way it is still compliant. 

Anyway, the system itself has three layers: interfaces/incoming, domain and interfaces/outcoming. Classes located in any of these packages are allowed to see any of the classes in its own layer or classes in the underlying layer, but not classes below this underlying layer. So, the most inner layer in this architecture is interfaces/outcoming, the middle layer is domain and the outer layer is interfaces/incoming. 

## Reactive programming

This system benefits from [Reactive Programming](https://en.wikipedia.org/wiki/Reactive_programming), by using Spring WebFlux in every layer. The exception to this rule are repositories (refer to [this ADR doc](https://github.com/alesaudate/car-app/blob/master/doc/adr/0002-remove-r2dbc.md) to understand why).

## Unit / Integration / Contract tests

This system applies the concept of a [tests pyramid](https://martinfowler.com/articles/practical-test-pyramid.html), although it doesn't have built-in end-to-end tests and, obviously enough, no UI tests. It doesn't have automated end-to-end tests because, afaik, it should be done in the system once it is "live", like for validation of its health while on production or in pre-production environments (validating blue/green deployments or canary deployments, for example). 

Anyhow, I'd like to point out that I really like [Runscope](https://www.runscope.com/) to do this type of stuff. 

## Security check on dependencies

This system has an automated verifier for bugs in dependencies, using a OWASP plugin. 

## linter for Kotlin

It has a linter in place. Have a look in (/gradle/lint.gradle) for details.

## Full coverage, listed by several different views

It uses JaCoCo to calculate full coverage . JaCoCo actually has several dimensions to look at coverage, and it is done according to [this gradle script](https://github.com/alesaudate/car-app/blob/master/gradle/coverage.gradle), which is configured through [this variables file](https://github.com/alesaudate/car-app/blob/master/gradle/variables.gradle). 

It also has several mutation tests in place, running through a [PiTest plugin](https://pitest.org/). 


## How to run it

There's a docker-compose file inside the `docker` folder. Just do a `docker-compose up -d` command in this folder and, once it is up, `./gradlew bootRun --args="--app.interfaces.outcoming.gmaps.appKey=<YOUR API KEY>"`. 

Then, there's a Postman file on the root, which is completely documented and should be a good guide on how to use the system. Alternatively, you can also refer to [Swagger](http://localhost:8080/swagger-ui.html).
