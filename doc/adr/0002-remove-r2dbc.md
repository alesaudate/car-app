# 2. Remove R2DBC

Date: 2021-05-14

## Status

Accepted

## Context

In the version with R2DBC, creating a travel request would raise the following exception:

``
org.springframework.dao.InvalidDataAccessApiUsageException: Nested entities are not supported
at org.springframework.data.r2dbc.convert.MappingR2dbcConverter.writePropertyInternal(MappingR2dbcConverter.java:405) ~[spring-data-r2dbc-1.2.8.jar:1.2.8]
Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException:
Error has been observed at the following site(s):
|_ checkpoint ⇢ Handler com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.TravelRequestAPI#makeTravelRequest(TravelRequestInput) [DispatcherHandler]
|_ checkpoint ⇢ HTTP POST "/travel-requests" [ExceptionHandlingWebHandler]
``

Observe the message `Nested entities are not supported`. It means that currently Spring Data R2DBC does not support relationships between entities, as it's the case between travel requests and passengers.

## Decision

We will need to revert it back to Spring Data with JPA, in order to reestablish such relationships.

## Consequences

The database interactions will not be reactive anymore, therefore we will need some arrangement to make it interact well with the other components.
