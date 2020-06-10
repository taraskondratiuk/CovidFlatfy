package com.example.service

import com.example.dto.CovidCase

import scala.io.Source

class CovidCasesService {
  def getKyivCovidCasesMap(covidDataFile: String): Map[String, (Int, Set[String])] = {
    val src = Source.fromFile(covidDataFile)
    src
      .getLines()
      .drop(1)
      .map(_.split("\",\""))
      .filter(_.length >= 5)
      .filter(_(4) == "м.Київ")
      .map{ arr =>
        val street = arr(3).toLowerCase.split(",")
          .find(text => text.matches(".*(вул|пр-т|пров|шосе|б-р|просп|пр).*"))
        if (street.isDefined){
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
  }
}

object CovidCasesService {
  def apply(): CovidCasesService = new CovidCasesService()
}
