package com.example.service

import java.io.File

import com.example.utility.FileDownloader
import org.scalatest.FunSuite

import scala.collection.mutable
import scala.language.postfixOps

class CovidCasesServiceTest extends FunSuite with FileDownloader {
  
  test("FileDownloader should download file") {
    val path = sys.env("PROJECT_PATH") + "\\data\\data.csv"
    new File(path).delete()
    
    downloadFile(sys.env("COVID_DATA_URI"), path)
    assert(new File(path).exists())
  }
  
  
  test("CovidCasesService should return valid result") {
    val service = CovidCasesService()
    service.refreshMap(sys.env("PROJECT_PATH") + "\\src\\test\\resources\\testdata.csv")

    val actual: mutable.Map[String, (Int, Set[String])] =
      service.kyivCovidCasesMap

    val expected: Map[String, (Int, Set[String])] = Map(
      "богатирська" -> (4, Set("30.03.2020 0:00:00", "31.03.2020 0:00:00")),
      "порика" -> (1, Set("31.03.2020 0:00:00"))
    )
    
    assert(actual.toSet diff expected.toSet isEmpty)
    assert(expected.toSet diff actual.toSet isEmpty)
  }
}
