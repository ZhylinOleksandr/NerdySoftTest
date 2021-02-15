
FROM tomcat

COPY target/NerdySoftTestProject-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/NerdySoftTestProject-1.0-SNAPSHOT.war

WORKDIR /usr/local/tomcat

EXPOSE 8080


#FROM openjdk:8

#ENV MAVEN_VERSION 3.6.3


#RUN mkdir -p /usr/share/maven \
#  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
#    | tar -xzC /usr/share/maven --strip-components=1 \
#  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

#ENV MAVEN_HOME /usr/share/maven

#VOLUME /root/.m2

#CMD ["mvn"] 

#ENTRYPOINT ["java","NerdySoftTestProject"]