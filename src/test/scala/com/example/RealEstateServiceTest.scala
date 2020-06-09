package com.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.example.dto.RealEstate.RealEstate
import com.example.dto.RealEstateWithCovidCases
import com.example.service.{CovidCasesService, RealEstateService}
import org.scalatest.FunSuite

import scala.util.Success

class RealEstateServiceTest extends FunSuite {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  val service = RealEstateService(CovidCasesService().getKyivCovidCasesMap(sys.env("PROJECT_PATH") + "\\src\\test\\testdata.csv"))
  
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
    val realEstateList = service.getRealEstateRequestsSource(5).mapAsyncUnordered(8)(service.parseRequestIntoSeqOfRealEstate).runWith(Sink.seq[Seq[RealEstate]])
    var actual: Seq[Seq[RealEstate]] = null
    realEstateList.onComplete{x => actual = x.get}
    Thread.sleep(5000)
    assert(actual.size == 5)
    assert(actual.head.size == 30)
  }
  
  test("seqRealEstateToRealEstateWithCovidCasesFlow should run") {
    val res = service
      .getRealEstateRequestsSource(10)
      .mapAsyncUnordered(8)(service.parseRequestIntoSeqOfRealEstate)
      .via(service.seqRealEstateToRealEstateWithCovidCasesFlow)
      .runWith(Sink.seq[Option[RealEstateWithCovidCases]])
    Thread.sleep(10000)
  }
}
