# Use Ubuntu 25 if available; change ARG UBUNTU_TAG if you prefer another tag
ARG UBUNTU_TAG=25.04
FROM ubuntu:${UBUNTU_TAG}

ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

# Core system + dev packages + native libs often needed for cross-platform DL work
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    ca-certificates curl wget git sudo gnupg lsb-release \
    build-essential cmake ninja-build unzip zip pkg-config \
    python3 python3-dev python3-pip python3-venv python3-setuptools \
    libssl-dev zlib1g-dev libffi-dev libbz2-dev liblzma-dev \
    libncurses5-dev libncursesw5-dev libreadline-dev libsqlite3-dev \
    libpng-dev libjpeg-dev libsndfile1-dev libopencv-dev \
    libopenblas-dev liblapack-dev libatlas-base-dev procps file rsync iproute2 locales \
 && locale-gen en_US.UTF-8 \
 && update-ca-certificates \
 && rm -rf /var/lib/apt/lists/*

ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8

# Create non-root user (vscode) for Codespaces / devcontainer use
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=1000
RUN groupadd --gid $USER_GID $USERNAME || true \
 && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME -s /bin/bash $USERNAME \
 && echo "$USERNAME ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/$USERNAME \
 && chmod 0440 /etc/sudoers.d/$USERNAME

# Install Eclipse Temurin (Adoptium) JDK 21 (stable dev JDK)
ARG TEMURIN_BASE_URL=https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk
RUN set -eux; \
    mkdir -p /opt/temurin; cd /tmp; \
    curl -fSL "$TEMURIN_BASE_URL" -o temurin-jdk.tar.gz; \
    tar -xzf temurin-jdk.tar.gz -C /opt/temurin --strip-components=1; \
    rm temurin-jdk.tar.gz; \
    update-alternatives --install /usr/bin/java java /opt/temurin/bin/java 100 \
                        --slave /usr/bin/javac javac /opt/temurin/bin/javac \
                        --slave /usr/bin/jar jar /opt/temurin/bin/jar

ENV JAVA_HOME=/opt/temurin
ENV PATH="$JAVA_HOME/bin:$PATH"

# Switch to non-root user
USER $USERNAME
WORKDIR /workspace

# Python tooling upgrade
RUN python3 -m pip install --upgrade pip wheel setuptools

# Create caches & common directories (Gradle, Maven, etc.)
RUN mkdir -p /workspace/.cache \
 && mkdir -p /home/$USERNAME/.gradle \
 && mkdir -p /home/$USERNAME/.m2 \
 && chown -R $USERNAME:$USERNAME /home/$USERNAME/.gradle /home/$USERNAME/.m2 /workspace

CMD ["/bin/bash"]
