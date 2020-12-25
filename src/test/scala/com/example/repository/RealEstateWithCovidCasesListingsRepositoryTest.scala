package com.example.repository

import java.text.SimpleDateFormat
import java.util.Date
import com.example.model.RealEstateWithCovidCases
import org.scalatest.FunSuite

class RealEstateWithCovidCasesListingsRepositoryTest extends FunSuite {
  val repo = RealEstateWihtCovidCasesListingsRepository(sys.env("DB_HOST"), sys.env("DB_PORT").toInt)
  
  test("getLastListing should work properly") {
    val listing = Seq(RealEstateWithCovidCases("їїї", "", "\"їїї\"", "sdf", 3, Set("3", "волад")))
  
    val dateFormat = new SimpleDateFormat("yyyy:MM:dd:hh mm")
    val date = new Date()
  
    val lastKey = repo.r.get("lastKey")
    repo.saveListing(listing)
    val newListing = repo.getLastListing()
    assert(listing == newListing.data)
    repo.r.del(dateFormat.format(date))
    repo.r.set("lastKey", lastKey)
  }
}
