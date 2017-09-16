FROM openjdk:8-jdk

MAINTAINER Andrii Bakal <andrew.bakal@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000", "-jar", "/usr/share/neuralswarm/ns.jar"]

ADD "fetchSo.sh" /scripts/fetchSo.sh
RUN bash /scripts/fetchSo.sh

ADD target/lib /usr/share/neuralswarm/lib
ADD "target/neuralswarm-1.0-SNAPSHOT.jar" /usr/share/neuralswarm/ns.jar
