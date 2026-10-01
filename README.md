# java-spring-wuphf

A simple Spring Boot demo application.

It is written in Kotlin, counts how often it has been visited
and shows the environment it runs in, dressed up as the fictional company WUPHF.com. Run on
its own, it keeps the count in memory. Given a PostgreSQL database, it
keeps the count in that database.

## Prerequisites

- [mise](https://mise.jdx.dev), which installs the JDK the build needs (Temurin 21)
- Docker, for the tests that run against PostgreSQL

Everything else, Gradle included, comes with the repository.

```shell
mise install
```

## Building

```shell
mise run build
```

This produces two artifacts:

| Artifact                                          | What it is                                                  |
|---------------------------------------------------|-------------------------------------------------------------|
| `build/libs/java-spring-wuphf.jar`          | An executable jar with every dependency in it               |
| `build/distributions/java-spring-wuphf.zip` | Start scripts and the dependencies as separate jars in `lib/` |

The zip unpacks to one directory:

```
java-spring-wuphf/
  bin/java-spring-wuphf        (and .bat for Windows)
  lib/*.jar
```

## Docker image

```shell
mise run image
docker run --rm -p 8080:8080 java-spring-wuphf:latest
```

Gradle builds the image with Spring Boot's `bootBuildImage`, from Cloud Native Buildpacks, so there
is no Dockerfile. It is tagged `latest` and with the project version.

Every push to `main` builds and tests the application on GitHub Actions and then publishes the image
to `ghcr.io/pellepelster/java-spring-wuphf`:

```shell
docker run --rm -p 8080:8080 ghcr.io/pellepelster/java-spring-wuphf:latest
```

## Testing

```shell
mise run test
```

This runs the unit tests and the integration tests. The integration tests start the whole
application twice: once without a database, and once against PostgreSQL in a
[Testcontainers](https://testcontainers.com) container, so Docker has to be running.

## Running

From the executable jar:

```shell
java -jar build/libs/java-spring-wuphf.jar
```

From the distribution:

```shell
unzip build/distributions/java-spring-wuphf.zip
./java-spring-wuphf/bin/java-spring-wuphf
```

Or straight from the sources, without packaging:

```shell
mise exec -- ./gradlew bootRun
```

The application listens on port 8080. Open http://localhost:8080 in a browser, or ask for JSON:

```shell
curl -H 'Accept: application/json' localhost:8080/
```

### Running with a database

The application switches to PostgreSQL when these variables are set:

| Variable            | Value                                |
|---------------------|--------------------------------------|
| `DATABASE_HOST`     | The host the database runs on        |
| `DATABASE_PORT`     | The port it listens on               |
| `DATABASE_NAME`     | The name of the database             |
| `DATABASE_USER`     | The user the application connects as |
| `DATABASE_PASSWORD` | That user's password                 |

`DATABASE_HOST` decides it: when it is set, the application starts with the `database`
profile and connects; when it is not, it starts with the `memory` profile and no datasource at all.

To try it locally, start a PostgreSQL and hand the application the variables:

```shell
docker run --rm -d --name wuphf-postgres -p 5432:5432 \
  -e POSTGRES_DB=wuphf -e POSTGRES_USER=wuphf_admin -e POSTGRES_PASSWORD=wuphf \
  postgres:17-alpine

DATABASE_HOST=localhost \
DATABASE_PORT=5432 \
DATABASE_NAME=wuphf \
DATABASE_USER=wuphf_admin \
DATABASE_PASSWORD=wuphf \
java -jar build/libs/java-spring-wuphf.jar
```

Liquibase creates the `visits` table on start-up, one row per visit, so the count survives a
restart and every instance behind the same database counts together.

## What it shows

Every request to `/` records a visit and answers with:

- how many visits there have been
- where they are kept: in memory, or in the linked database with its host, port, database, user
  and table
- the environment variables of the process, with the database ones (`DATABASE_*`) listed
  first

Browsers and a plain `curl` get an HTML page. Only a request with an explicit
`Accept: application/json` gets JSON (shortened here):

```json
{
  "visited": 3,
  "storage": {
    "kind": "database",
    "name": "PostgreSQL",
    "persistent": true,
    "shared": true,
    "details": {
      "host": "localhost",
      "port": "5432",
      "database": "wuphf",
      "user": "wuphf_admin",
      "url": "jdbc:postgresql://localhost:5432/wuphf",
      "table": "visits"
    }
  },
  "environment": {
    "database": [
      { "name": "DATABASE_HOST", "value": "localhost", "secret": false },
      { "name": "DATABASE_PASSWORD", "value": "********", "secret": true }
    ],
    "other": [ ... ]
  }
}
```

The page is self-contained: its styles are inline and it loads no script, font or image, because
the machine it runs on may have no route to the internet.

## Secrets

A variable whose name contains `PASSWORD`, `PASSWD`, `SECRET`, `TOKEN`, `KEY`, `CREDENTIAL`,
`PRIVATE`, `AUTH`, `SIGNATURE`, `SALT` or `CERT` shows `********` instead of its value, both on the
page and in the JSON. Credentials in a database URL are masked the same way.

**The page has no authentication.** It shows the environment of its process because a demo should
show what a deployment hands to an application. Do not do this in a service that holds real data.
