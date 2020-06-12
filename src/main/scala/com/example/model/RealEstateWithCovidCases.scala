package main.scala.com.example.model

import spray.json.DefaultJsonProtocol

case class RealEstateWithCovidCases(street: String,
                                    priceSqm: String,
                                    updateTime: String,
                                    singleRealtyUrl: String,
                                    numCases: Int,
                                    casesDates: Set[String]
                                   )

object RealEstateWithCovidCasesProtocol extends DefaultJsonProtocol {
  implicit val realEstateWithCovidCasesFormat = jsonFormat6(RealEstateWithCovidCases)
}