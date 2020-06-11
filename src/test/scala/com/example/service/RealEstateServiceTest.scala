package com.example.service

import akka.actor.ActorSystem
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.Sink
import com.example.dto.RealEstate.RealEstate
import com.example.dto.RealEstateWithCovidCases
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success

class RealEstateServiceTest extends FunSuite {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val service = RealEstateService(CovidCasesService().kyivCovidCasesMap)


  test("api call should return status 200") {
    val future = service.getFlatfyResponseFuture(1)
    var actual: Boolean = false
    Thread.sleep(5000)
    future.onComplete{case Success(value) => actual = value.status.isSuccess()}
    assert(actual)
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
    realEstateList.onComplete{x => actual = x.get}
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
    Thread.sleep(10000)
  }

  test("getTopTenRealEstateWithCovidCasesByPriceSqm should not fall with exception") {
    val actual = service.getTopTenRealEstateWithCovidCasesByPriceSqm(parallelism = 1, numPages = 10)
  }
}