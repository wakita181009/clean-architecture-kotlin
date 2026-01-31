FROM eclipse-temurin:25-jre
COPY ./framework/build/libs/framework-0.0.1.jar /clean-architecture-kotlin.jar
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /clean-architecture-kotlin.jar \"$@\"", "--"]
