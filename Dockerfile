# 使用 AWS ECR Public 的 Amazon Corretto 21，避免 CodeBuild 从 Docker Hub 拉 Java 镜像时遇到匿名限流。
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21 AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

# runtime 阶段也使用 ECR Public 的 Java 21 镜像，保持和 build 阶段同一个发行版。
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
