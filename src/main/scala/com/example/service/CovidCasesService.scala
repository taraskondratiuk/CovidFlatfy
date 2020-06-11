package com.example.service

import com.example.dto.CovidCase

import scala.io.Source

class CovidCasesService(val kyivCovidCasesMap: scala.collection.concurrent.Map[String, (Int, Set[String])]) {
  def refreshMap(covidDataFile: String): Unit = {
    val src = Source.fromFile(covidDataFile)
    val newMap = src
      .getLines()
      .drop(1)
      .map(_.split("\",\""))
      .filter(_.length >= 5)
      .filter(_ (4) == "м.Київ")
      .map { arr =>
        val street = arr(3).toLowerCase.split(",")
          .find(text => text.matches(".*(вул|пр-т|пров|шосе|б-р|просп|пр).*"))
        if (street.isDefined) {
          Option(CovidCase(street.get
            .replaceAll("вул|пр-т|пров|шосе|б-р|просп|пр|\\.|", "").trim, arr(5)))
        } else Option.empty
        
      }
      .filter(_.isDefined)
      .map(_.get)
      .toList
      .groupMap(_.address)(_.date)
      .view
      .mapValues(list => (list.length, list.toSet))
      .toMap
    kyivCovidCasesMap ++ newMap
  }
}

object CovidCasesService {
  def apply(
             kyivCovidCasesMap: scala.collection.concurrent.Map[String, (Int, Set[String])] =
             scala.collection.concurrent.TrieMap[String, (Int, Set[String])]()
           ): CovidCasesService = new CovidCasesService(kyivCovidCasesMap)
}
