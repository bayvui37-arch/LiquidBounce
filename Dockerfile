FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# Cài đặt các dependencies cần thiết
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y \
    curl \
    wget \
    git \
    zip \
    unzip \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt Java 21
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk openjdk-21-jre && \
    apt-get clean

# Thiết lập environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Xác minh cài đặt
RUN java -version && javac -version

# Tạo workspace
WORKDIR /workspace

# Copy project files (Codespace sẽ mount tự động)
COPY . .

# Thiết lập quyền execute cho gradlew
RUN chmod +x gradlew
