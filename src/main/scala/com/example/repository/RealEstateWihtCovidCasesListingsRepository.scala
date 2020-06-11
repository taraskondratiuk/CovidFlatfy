package com.example.repository

import java.text.SimpleDateFormat
import java.util.Date

import com.example.dto.RealEstateWithCovidCases
import com.example.utility.Serializer
import redis.clients.jedis.Jedis


class RealEstateWihtCovidCasesListingsRepository(val host: String, val port: Int) extends Serializer {
  val r = new Jedis(host, port)

  def saveListing(seq: Seq[RealEstateWithCovidCases]): Unit = {
    val dateFormat= new SimpleDateFormat("yyyy:MM:dd:hh mm")
    r.set(serialize(dateFormat.format(new Date())), serialize(seq))
  }
  
  def getThisDayListings(): Map[String ,Seq[RealEstateWithCovidCases]] = {
    val dateFormat = new SimpleDateFormat("yyy:MM:dd:*")
    
    val keys = r.keys(dateFormat.format(new Date())).toArray(Array[String]()).sorted(Ordering.String.reverse)
    keys.map(key => (key, deserialize(r.get(serialize(key))).asInstanceOf[Seq[RealEstateWithCovidCases]])).toMap
  }
}

object RealEstateWihtCovidCasesListingsRepository {
  def apply(host: String, port: Int): RealEstateWihtCovidCasesListingsRepository =
    new RealEstateWihtCovidCasesListingsRepository(host, port)
}
