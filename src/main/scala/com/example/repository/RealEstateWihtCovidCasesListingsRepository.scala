package com.example.repository

import java.text.SimpleDateFormat
import java.util.Date
import com.example.model.{DateWithRealEstateWithCovidCases, RealEstateWithCovidCases}
import redis.clients.jedis.Jedis
import com.example.model.RealEstateWithCovidCasesProtocol._
import spray.json._

class RealEstateWihtCovidCasesListingsRepository(val host: String, val port: Int) {
  val r = new Jedis(host, port)
  
  def saveListing(seq: Seq[RealEstateWithCovidCases]): Unit = {
    val dateFormat = new SimpleDateFormat("yyyy:MM:dd:hh mm")
    val date = new Date()

    r.set(dateFormat.format(date), seq.toJson.compactPrint)
    r.set("lastKey", dateFormat.format(date))
  }
  
  def getThisDayListings(): Seq[DateWithRealEstateWithCovidCases] = {
    val dateFormat = new SimpleDateFormat("yyy:MM:dd:*")
    
    val keys = r.keys(dateFormat.format(new Date())).toArray(Array[String]()).sorted(Ordering.String.reverse)

    keys.map(key => DateWithRealEstateWithCovidCases(key, r.get(key).parseJson.convertTo[Seq[RealEstateWithCovidCases]]))
  }
  
  def getLastListing(): DateWithRealEstateWithCovidCases = {
    val lastKey = r.get("lastKey")
    if (lastKey != null) {
      DateWithRealEstateWithCovidCases(lastKey, r.get(lastKey).parseJson.convertTo[Seq[RealEstateWithCovidCases]])
    } else DateWithRealEstateWithCovidCases("", Seq.empty)
  }
}

object RealEstateWihtCovidCasesListingsRepository {
  def apply(host: String, port: Int): RealEstateWihtCovidCasesListingsRepository =
    new RealEstateWihtCovidCasesListingsRepository(host, port)
}