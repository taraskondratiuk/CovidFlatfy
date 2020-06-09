package com.example.dto

case class RealEstateWithCovidCases(street: String,
                                    priceSqm: String,
                                    updateTime: String,
                                    singleRealtyUrl: String,
                                    numCases: Int,
                                    casesDates: Set[String]
                                   )
