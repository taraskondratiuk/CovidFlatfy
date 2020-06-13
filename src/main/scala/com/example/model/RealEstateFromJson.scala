package com.example.model

import spray.json._

object RealEstateFromJson extends DefaultJsonProtocol {
  case class RealEstate(geo: Option[Geo], priceSqm: Option[String], updateTime: Option[String], singleRealtyUrl: Option[String])
  
  case class Street(name: Option[String], nameFull: Option[String])
  
  case class Address(street: Option[Street])
  
  case class Geo(address: Option[Address])
  
  implicit val streetFormat = jsonFormat(Street, "name", "nameFull")
  
  implicit val addressFormat = jsonFormat(Address, "street")
  implicit val geoFormat = jsonFormat(Geo, "address")
  implicit val realEstateFormat = jsonFormat(RealEstate, "geo", "priceSqm", "updateTime", "singleRealtyUrl")
}

