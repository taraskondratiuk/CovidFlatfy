FROM mozilla/sbt

COPY . /CovidFlatfy

WORKDIR /CovidFlatfy

EXPOSE 8080

CMD sbt run
