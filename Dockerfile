# syntax=docker/dockerfile:1

# Build
#   docker build --tag=fcs-endpoint-tester .

# Run
#   docker run -p 8080:8080 fcs-endpoint-tester

# ---------------------------------------------------------------------------
FROM maven:3.8.6-jdk-8 AS deps

WORKDIR /work

RUN git clone --depth 1 https://github.com/clarin-eric/fcs-sru-client.git && \
    cd fcs-sru-client/ && \
    rm -rf .git/ && \
    mvn -q install && \
    cd .. && \
    rm -rf fcs-sru-client

RUN git clone --depth 1 https://github.com/clarin-eric/fcs-simple-client.git && \
    cd fcs-simple-client/ && \
    rm -rf .git/ && \
    mvn -q install && \
    cd .. && \
    rm -rf fcs-simple-client

# ---------------------------------------------------------------------------
FROM maven:3.8.6-jdk-8 AS war

WORKDIR /work

COPY --from=deps /root/.m2/repository/eu/clarin/sru /root/.m2/repository/eu/clarin/sru

COPY . /work/

RUN mvn package

# ---------------------------------------------------------------------------
FROM tomcat:8-jre8-temurin-jammy as run

COPY --from=war /work/target/FCSEndpointTester*.war $CATALINA_HOME/webapps/ROOT.war
