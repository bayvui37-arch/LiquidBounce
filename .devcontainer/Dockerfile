# Sử dụng Ubuntu 25.04 (khi ra mắt) hoặc Ubuntu 24.04 hiện tại
FROM ubuntu:24.04

# Thiết lập biến môi trường
ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Cập nhật hệ thống và cài đặt các package cần thiết
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y \
    curl \
    wget \
    git \
    vim \
    nano \
    build-essential \
    software-properties-common \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Thêm repository Java và cài đặt Java 21
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk openjdk-21-jre && \
    apt-get clean

# Xác minh Java installation
RUN java -version && javac -version

# Tạo thư mục workspace
RUN mkdir -p /workspace
WORKDIR /workspace

# Thiết lập shell mặc định
SHELL ["/bin/bash", "-c"]
