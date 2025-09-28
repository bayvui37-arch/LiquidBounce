# Ubuntu rolling release (bản mới nhất)
FROM ubuntu:rolling

# Cài đặt Java 21 (Temurin) và các công cụ dev
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    wget curl git unzip zip tar \
    build-essential cmake ninja-build pkg-config \
    python3 python3-pip \
    libssl-dev libffi-dev libzstd-dev liblz4-dev libbz2-dev \
    libpng-dev libjpeg-dev libtiff-dev libxml2-dev libxslt1-dev \
    libgl1-mesa-dev libglu1-mesa-dev libx11-dev libxext-dev libxrender-dev \
    libxtst-dev libxrandr-dev \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Cài Java 21 Temurin
RUN wget -O- https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/trusted.gpg.d/adoptium.asc \
    && echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y temurin-21-jdk \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /workspace
