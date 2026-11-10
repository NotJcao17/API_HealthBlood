# ---------- Etapa 1: compilar el codigo Java ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY lib ./lib
COPY src ./src
COPY web ./web

# Compila las clases y arma la carpeta de la aplicacion web (igual que el .war de NetBeans)
RUN mkdir -p webapp \
 && cp -r web/. webapp/ \
 && mkdir -p webapp/WEB-INF/classes webapp/WEB-INF/lib \
 && cp lib/*.jar webapp/WEB-INF/lib/ \
 && javac --release 17 -encoding UTF-8 -nowarn \
      -cp "lib/*" \
      -d webapp/WEB-INF/classes \
      $(find src/java -name "*.java")

# ---------- Etapa 2: servidor Tomcat 11 ----------
FROM tomcat:11.0-jdk21-temurin

# Quita las apps de ejemplo de Tomcat y publica la API en la raiz (/)
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/webapp /usr/local/tomcat/webapps/ROOT

EXPOSE 8080
CMD ["catalina.sh", "run"]
