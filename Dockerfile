FROM eclipse-temurin:25-jre
COPY ./framework/build/libs/framework-0.0.1.jar /clean-architecture-kotlin.jar
ENTRYPOINT ["java", "-jar", "/clean-architecture-kotlin.jar"]
