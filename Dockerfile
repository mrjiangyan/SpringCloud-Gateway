#FROM openjdk:17-jdk
FROM registry.cn-hangzhou.aliyuncs.com/aiprime-backend-v2/java:apache-beam_java17_sdk
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai  /etc/localtime
#EXPOSE 18000

RUN ls -l /

RUN ls -l ./

# 移动文件到目标文件夹
ARG APP_LOCATION=target/gateway.jar

#ENV APP_HOME /home/admin/app/

ADD ${APP_LOCATION} /

