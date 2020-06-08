package com.example

import com.example.service.CovidCasesService
import com.example.utility.FileDownloader
import org.scalatest.FunSuite

class CovidCasesServiceTest extends FunSuite {
  
  test("FileDownloader should download file without exceptions") {
    val fileDownloader = FileDownloader()
    fileDownloader.downloadFile(sys.env("COVID_DATA_URL"), sys.env("COVID_DATA_PATH"))
  }
  
  
  test("CovidCasesService should return valid result") {
    val service = CovidCasesService()
    
    val actual: Map[String, (Int, Set[String])] =
      service.getKyivCovidCasesMap(sys.env("PROJECT_PATH") + "src\\test\\testdata.csv")
    
    val expected: Map[String, (Int, Set[String])] = Map(
      "вул богатирська" -> (4, Set("30.03.2020 0:00:00", "31.03.2020 0:00:00")),
      "вул порика" -> (1, Set("31.03.2020 0:00:00"))
    )
    assert(actual.toSet diff expected.toSet isEmpty)
  }
}
