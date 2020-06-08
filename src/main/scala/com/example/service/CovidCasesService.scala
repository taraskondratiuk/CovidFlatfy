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
      .filter(_(3) == "м.Київ")
      .map(arr => CovidCase(arr(2).toLowerCase().replace(".,'\"`?!", ""), arr(3)))
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
