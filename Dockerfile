FROM soleo/alpine-scala

WORKDIR /code
ADD app /code/app
ADD conf /code/conf
ADD public /code/public
ADD project/plugins.sbt /code/project/plugins.sbt
ADD project/build.properties /code/project/build.properties
ADD project/sbt-ui.sbt /code/project/sbt-ui.sbt
ADD build.sbt /code/build.sbt

EXPOSE 80
ENV PORT 80

RUN ["sbt", "compile"]

CMD ["target/universal/stage/bin/scim-rest", "-Dhttp.port=${PORT} -Dplay.evolutions.db.default.autoApply=true -Dplay.evolutions.db.default.autoApplyDowns=true -Ddb.default.url=${JAWSDB_URL}"]