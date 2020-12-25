package com.example.model

import spray.json.DefaultJsonProtocol

case class RealEstateWithCovidCases(street: String,
                                    priceSqm: String,
                                    updateTime: String,
                                    singleRealtyUrl: String,
                                    numCases: Int,
                                    casesDates: Set[String])

case class DateWithRealEstateWithCovidCases(date: String,
                                            data: Seq[RealEstateWithCovidCases])

object RealEstateWithCovidCasesProtocol extends DefaultJsonProtocol {
  implicit val realEstateWithCovidCasesFormat = jsonFormat6(RealEstateWithCovidCases)
  implicit val dateWithRealEstateWithCovidCasesFormat = jsonFormat2(DateWithRealEstateWithCovidCases)
}
