FROM soleo/alpine-scala:2.11.7

WORKDIR /code

VOLUME  /code

ADD app /code/app
ADD conf /code/conf
ADD public /code/public
ADD project/plugins.sbt /code/project/plugins.sbt
ADD project/build.properties /code/project/build.properties
ADD project/sbt-ui.sbt /code/project/sbt-ui.sbt
ADD build.sbt /code/build.sbt


ENV ACTIVATOR_VERSION 1.3.9
# Set up activator
RUN cd "/tmp" && \
    wget http://downloads.typesafe.com/typesafe-activator/$ACTIVATOR_VERSION/typesafe-activator-$ACTIVATOR_VERSION-minimal.zip && \
    unzip typesafe-activator-$ACTIVATOR_VERSION-minimal.zip && \
    rm -f typesafe-activator-$ACTIVATOR_VERSION-minimal.zip && \
    mkdir /usr/share/activator && \
    rm "/tmp/activator-$ACTIVATOR_VERSION-minimal/bin/"*.bat && \
    mv "/tmp/activator-$ACTIVATOR_VERSION-minimal/bin" "/tmp/activator-$ACTIVATOR_VERSION-minimal/libexec" "/usr/share/activator" && \
    ln -s "/usr/share/activator/bin/"* "/usr/bin/" && \
    rm -rf "/tmp/"*

# Build the entire app so that all the dependencies are downloaded and all the
# code is compiled. This will make starting the app the first time much faster.
RUN activator dist

# Expose play port
EXPOSE 9000
EXPOSE 3306

# Default command is to run the app
CMD ["activator", "run"]
