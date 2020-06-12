package com.example.service

import akka.actor.ActorSystem
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.Sink
import com.example.dto.RealEstateFromJson.RealEstate
import com.example.dto.RealEstateWithCovidCases
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RealEstateServiceTest extends FunSuite {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  val covidCasesService = CovidCasesService()
  covidCasesService.refreshMap(sys.env("PROJECT_PATH") + "\\data\\data.csv")
  val service = RealEstateService(covidCasesService.kyivCovidCasesMap)
  
  test("api call should return status 200") {
    val future = service.getFlatfyResponseFuture(1)
    Await.result(future, Duration.Inf)
    assert(future.value.get.get.status.isSuccess())
  }
  
  test("countNumOfPages should return int num of pages") {
    val numOfPages = service.countNumOfPages()
    assert(numOfPages.isInstanceOf[Int])
    assert(numOfPages > 1000)
  }
  
  test("getFutureOfRealEstateSource should return valid source") {
    val realEstateList = service.getRealEstateRequestsSource(5)
      .mapAsyncUnordered(8)(service.parseRequestIntoSeqOfRealEstate).runWith(Sink.seq[Seq[RealEstate]])
    var actual: Seq[Seq[RealEstate]] = null
    realEstateList.onComplete { x => actual = x.get }
    Await.result(realEstateList, Duration.Inf)
    Thread.sleep(100)
    assert(actual.size == 5)
  }
  
  test("seqRealEstateToRealEstateWithCovidCasesFlow should not fall with exception") {
    val res = service
      .getRealEstateRequestsSource(10)
      .mapAsyncUnordered(8)(service.parseRequestIntoSeqOfRealEstate)
      .via(service.seqRealEstateToRealEstateWithCovidCasesFlow)
      .withAttributes(supervisionStrategy(resumingDecider))
      .runWith(Sink.seq[RealEstateWithCovidCases])
    Thread.sleep(3000)
  }
  
  test("getTopTenRealEstateWithCovidCasesByPriceSqm should not fall with exception") {
    val actual = service.getTopTenRealEstateWithCovidCasesByPriceSqm(parallelism = 1, numPages = 100)
    assert(actual.size == 10)
  }
}
