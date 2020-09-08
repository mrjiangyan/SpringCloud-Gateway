FROM registry.cn-beijing.aliyuncs.com/hub-mirrors/maven:3-jdk-8 AS build  
COPY ../src /usr/src/app/src
COPY run.sh usr/src/app/run.sh
COPY ../pom.xml /usr/src/app
COPY settings.xml /user/src/app/settings.xml
RUN mvn -f /usr/src/app/pom.xml -s /user/src/app/settings.xml clean package -DskipTests

from registry.cn-beijing.aliyuncs.com/hub-mirrors/openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/api-gateway.jar
COPY --from=build /usr/src/app/${JAR_FILE} app.jar
COPY --from=build /usr/src/app/run.sh run.sh

ARG profile=pat

ENV SPRING_PROFILES_ACTIVE ${profile}
ENTRYPOINT $profile
ENTRYPOINT chmod -R +777 /run.sh
ENTRYPOINT ["sh /run.sh",$profile]
