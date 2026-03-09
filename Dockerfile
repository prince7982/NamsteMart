FROM tomcat:9-jdk17

COPY target/NamasteMart-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
