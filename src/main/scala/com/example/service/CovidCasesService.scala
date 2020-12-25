package com.example.service

import com.example.model.CovidCase

import scala.io.Source

class CovidCasesService(val kyivCovidCasesMap: scala.collection.concurrent.Map[String, (Int, Set[String])]
                        = scala.collection.concurrent.TrieMap[String, (Int, Set[String])]()) {
  /**
   * method that refreshes in-memory map by reading covid cases data from given local csv  file
   * @param covidDataFile file path of local csv file with covid data
   */
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
    kyivCovidCasesMap ++= newMap
  }
}

object CovidCasesService {
  def apply(): CovidCasesService = new CovidCasesService()
  
  def apply(covidDataFile: String): CovidCasesService = {
    val service = new CovidCasesService()
    service.refreshMap(covidDataFile)
    service
  }
}
