package com.example.dto

import spray.json._

object RealEstate extends DefaultJsonProtocol {
  case class RealEstate(geo: Geo, priceSqm: String, updateTime: String, singleRealtyUrl: String)
  
  case class Street(name: String, nameFull: String)
  
  case class Address(street: Option[Street])
  
  case class Geo(address: Address)
  
  implicit val streetFormat = jsonFormat(Street, "name", "nameFull")
  
  implicit val addressFormat = jsonFormat(Address, "street")
  implicit val geoFormat = jsonFormat(Geo, "address")
  implicit val realEstateFormat = jsonFormat(RealEstate, "geo", "priceSqm", "updateTime", "singleRealtyUrl")
}

