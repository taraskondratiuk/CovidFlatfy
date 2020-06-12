package main.scala.com.example.repository

import java.text.SimpleDateFormat
import java.util.Date

import main.scala.com.example.model.RealEstateWithCovidCases
import main.scala.com.example.utility.Serializer
import redis.clients.jedis.Jedis


class RealEstateWihtCovidCasesListingsRepository(val host: String, val port: Int) extends Serializer {
  val r = new Jedis(host, port)
  
  def saveListing(seq: Seq[RealEstateWithCovidCases]): Unit = {
    val dateFormat = new SimpleDateFormat("yyyy:MM:dd:hh mm")
    val date = new Date()

    r.set(dateFormat.format(date).getBytes(), serialize(seq))
    r.set("lastKey", dateFormat.format(date))
  }
  
  def getThisDayListings(): Map[String, Seq[RealEstateWithCovidCases]] = {
    val dateFormat = new SimpleDateFormat("yyy:MM:dd:*")
    
    val keys = r.keys(dateFormat.format(new Date())).toArray(Array[String]()).sorted(Ordering.String.reverse)

    keys.map(key => (key, deserialize(r.get(key.getBytes())).asInstanceOf[Seq[RealEstateWithCovidCases]])).toMap
  }
  
  def getLastListing(): (String, Seq[RealEstateWithCovidCases]) = {
    val lastKey = r.get("lastKey")
    if (!lastKey.isEmpty) {
      (lastKey, deserialize(r.get(lastKey.getBytes)).asInstanceOf[Seq[RealEstateWithCovidCases]])
    } else ("", Seq())
  }
}

object RealEstateWihtCovidCasesListingsRepository {
  def apply(host: String, port: Int): RealEstateWihtCovidCasesListingsRepository =
    new RealEstateWihtCovidCasesListingsRepository(host, port)
}