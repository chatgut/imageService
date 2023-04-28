FROM ubuntu:latest
LABEL authors="mikaeledwartz"
COPY . /app
WORKDIR /app
RUN mvn clean package

ENTRYPOINT ["top", "-b"]
