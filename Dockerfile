FROM picoded/ubuntu-openjdk-8-jdk

MAINTAINER Andrii Bakal <andrew.bakal@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000", "-jar", "/usr/share/neuralswarm/ns.jar", "-Djava.library.path=/usr/lib"]

ADD target/lib /usr/share/neuralswarm/lib
ADD "target/neuralswarm-1.0-SNAPSHOT.jar" /usr/share/neuralswarm/ns.jar

ADD libopencv_java320.so /usr/lib/libopencv_java320.so
ADD netFile /network
