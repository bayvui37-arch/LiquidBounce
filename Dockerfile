FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# Cài đặt các package cần thiết bao gồm SSH
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y \
    curl \
    wget \
    git \
    zip \
    unzip \
    openssh-server \
    sudo \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt Java 21
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk openjdk-21-jre && \
    apt-get clean

# Tạo user vscode (required cho Codespaces)
RUN useradd -m -s /bin/bash vscode && \
    echo "vscode ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/vscode

# Thiết lập environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Cấu hình SSH
RUN mkdir -p /run/sshd && \
    ssh-keygen -A

WORKDIR /workspaces/LiquidBounce

# Xác minh cài đặt
RUN java -version && javac -version

CMD ["/usr/sbin/sshd", "-D"]
