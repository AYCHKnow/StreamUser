FROM java:openjdk-8
MAINTAINER SequenceIQ

ENV APP_ROOT /usr/src/app
ENV SRC_ROOT /usr/src/src
ENV SCALA_VERSION 2.11.1
ENV SBT_VERSION 0.13.13

RUN mkdir -p $APP_ROOT
RUN mkdir -p $SRC_ROOT

# Install Scala and sbt
RUN wget www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb
RUN dpkg -i scala-$SCALA_VERSION.deb
RUN apt-get update
RUN apt-get install scala

# Install sbt
RUN echo 'deb http://dl.bintray.com/sbt/debian /' > /etc/apt/sources.list.d/sbt.list &&\
    apt-get update && \
    apt-get install -y --force-yes sbt=$SBT_VERSION && \
    apt-get clean autoremove -y && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Preload sbt
COPY build.sbt $SRC_ROOT/build.sbt
COPY project/build.properties  $SRC_ROOT/project/build.properties
COPY project/plugins.sbt  $SRC_ROOT/project/plugins.sbt
RUN cd $SRC_ROOT; sbt

# Package app
COPY src $SRC_ROOT/src
RUN cd $SRC_ROOT; sbt universal:packageBin
RUN mv $SRC_ROOT/target/universal/deploy.zip $APP_ROOT/deploy.zip

RUN unzip $APP_ROOT/deploy.zip -d $APP_ROOT/ && rm $APP_ROOT/deploy.zip

# WORKDIR kept at bottom because it's breaking Docker cache.
WORKDIR $APP_ROOT
CMD bin/streaming-user-segmentation
