ARG VERSION=latest
FROM debian:buster-slim

# Install last updates
RUN apt update \
    && apt upgrade -y \
    && rm -rf /var/lib/apt/lists/*

RUN apt update \
    && apt install -y --no-install-recommends ca-certificates p11-kit netbase tzdata git curl \
	&& rm -rf /var/lib/apt/lists/*

# Install JDK 11
ENV JAVA_HOME /usr/local/openjdk-11
ENV PATH $JAVA_HOME/bin:$PATH
ENV LANG C.UTF-8
ENV JAVA_VERSION 11.0.14
RUN { echo '#/bin/sh'; echo 'echo "$JAVA_HOME"'; } > /usr/local/bin/docker-java-home \
    && chmod +x /usr/local/bin/docker-java-home \
    && [ "$JAVA_HOME" = "$(docker-java-home)" ] \
    && savedAptMark="$(apt-mark showmanual)" \
    && apt-get update \
	&& apt-get install -y --no-install-recommends wget gnupg dirmngr \
	&& rm -rf /var/lib/apt/lists/* \
    && wget --progress=dot:giga -O openjdk.tgz "https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.14%2B9/OpenJDK11U-jdk_x64_linux_11.0.14_9.tar.gz" \
    && wget --progress=dot:giga -O openjdk.tgz.asc "https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.14%2B9/OpenJDK11U-jdk_x64_linux_11.0.14_9.tar.gz.sign" \
    && export GNUPGHOME="$(mktemp -d)" \
    && gpg --batch --keyserver keyserver.ubuntu.com --recv-keys EAC843EBD3EFDB98CC772FADA5CD6035332FA671 \
        || gpg --batch --keyserver keys.openpgp.org --recv-keys EAC843EBD3EFDB98CC772FADA5CD6035332FA671 \
        || gpg --batch --keyserver pgp.mit.edu --recv-keys EAC843EBD3EFDB98CC772FADA5CD6035332FA671 \
    && gpg --batch --keyserver keyserver.ubuntu.com --keyserver-options no-self-sigs-only --recv-keys CA5F11C6CE22644D42C6AC4492EF8D39DC13168F \
        || gpg --batch --keyserver keys.openpgp.org --keyserver-options no-self-sigs-only --recv-keys CA5F11C6CE22644D42C6AC4492EF8D39DC13168F \
        || gpg --batch --keyserver pgp.mit.edu --keyserver-options no-self-sigs-only --recv-keys CA5F11C6CE22644D42C6AC4492EF8D39DC13168F \
    && gpg --batch --list-sigs --keyid-format 0xLONG CA5F11C6CE22644D42C6AC4492EF8D39DC13168F | tee /dev/stderr | grep '0xA5CD6035332FA671' | grep 'Andrew Haley' \
    && gpg --batch --verify openjdk.tgz.asc openjdk.tgz \
    && gpgconf --kill all \
    && rm -rf "$GNUPGHOME" \
    && mkdir -p "$JAVA_HOME" \
    && tar --extract --file openjdk.tgz --directory "$JAVA_HOME" --strip-components 1 --no-same-owner \
    && rm openjdk.tgz* \
    && apt-mark auto '.*' > /dev/null \
    && [ -z "$savedAptMark" ] || apt-mark manual $savedAptMark > /dev/null \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
    && { \
		      echo '#!/usr/bin/env bash'; \
		      echo 'set -Eeuo pipefail'; \
		      echo 'trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth "$JAVA_HOME/lib/security/cacerts"'; \
	  } > /etc/ca-certificates/update.d/docker-openjdk \
    && find "$JAVA_HOME/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf \
    && ldconfig \
    && java -Xshare:dump \
    && fileEncoding="$(echo 'System.out.println(System.getProperty("file.encoding"))' | jshell -s -)" \
    && [ "$fileEncoding" = 'UTF-8' ] \
    && rm -rf ~/.java \
    && echo "Java Compiler Version:" \
    && javac --version \
    && echo "Java Runtime Version:" \
    && java --version

# Install Gradle
# ENV GRADLE_HOME /opt/gradle
# ENV GRADLE_VERSION 7.4.2
# ARG GRADLE_DOWNLOAD_SHA256=29e49b10984e585d8118b7d0bc452f944e386458df27371b49b4ac1dec4b7fda
# RUN savedAptMark="$(apt-mark showmanual)" \
#	  && apt-get update \
#	  && apt-get install -y --no-install-recommends wget unzip \
#    && wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
#    && echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - \
#    && unzip gradle.zip \
#    && rm gradle.zip \
#    && mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
#    && ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
#    && apt-mark auto '.*' > /dev/null \
#    && apt-mark manual $savedAptMark \
#    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
#    && echo "Gradle Version:" \
#    && gradle --version \
#    && apt-get install -y iputils-ping
######################################

ENV 3DMM_STATS_JAVALIN_PORT=8421

# Copy backend
COPY 3DMM-all.jar /opt/backend/3DMM-all.jar
COPY config/app-config.template.json /config/app-config.json

EXPOSE $3DMM_STATS_JAVALIN_PORT

# Check version endpoint
HEALTHCHECK CMD curl --fail http://localhost:$3DMM_STATS_JAVALIN_PORT/api/version || exit 1

CMD ["java", "-jar", "/opt/backend/3DMM-all.jar"]
