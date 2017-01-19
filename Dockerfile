FROM gettyimages/spark
ENV LD_LIBRARY_PATH $HADOOP_HOME/lib/native/:$LD_LIBRARY_PATH

ENV SRC_ROOT /usr/src/app
ENV SCALA_VERSION 2.11.1
ENV SBT_VERSION 0.13.13

RUN mkdir -p $SRC_ROOT
RUN apt-get update
RUN apt-get install -y wget

# Install Java
RUN echo deb http://http.debian.net/debian jessie-backports main >> /etc/apt/sources.list
RUN apt-get update && apt-get install -y openjdk-8-jdk
RUN update-alternatives --config java

# Install Scala
RUN wget www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb
RUN dpkg -i scala-$SCALA_VERSION.deb
RUN apt-get install -y scala

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

# Put packaged app in dist folder
RUN mkdir $SRC_ROOT/dist
RUN mv $SRC_ROOT/target/universal/deploy.zip $SRC_ROOT/dist/deploy.zip
RUN unzip $SRC_ROOT/dist/deploy.zip -d $SRC_ROOT/dist && rm $SRC_ROOT/dist/deploy.zip


# WORKDIR kept at bottom because it's breaking Docker cache.
WORKDIR $SRC_ROOT
RUN chmod +x dist/bin/streaming-user-segmentation
CMD /bin/bash dist/bin/streaming-user-segmentation
