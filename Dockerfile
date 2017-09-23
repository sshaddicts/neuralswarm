FROM picoded/ubuntu-openjdk-8-jdk

MAINTAINER Andrii Bakal <andrew.bakal@gmail.com>

RUN mkdir /varimg

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/neuralswarm/ns.jar", "-Djava.library.path=/usr/lib"]

ADD "target/neuralswarm-1.0-SNAPSHOT-jar-with-dependencies.jar" /usr/share/neuralswarm/ns.jar

ADD libopencv_java320.so /usr/lib/libopencv_java320.so
ADD netFile /network