package com.example

import akka.actor.ActorSystem
import com.example.service.RealEstateService
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success



class RealEstateServiceTest extends FunSuite {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  val service = RealEstateService()
  
  test("api call should return status 200") {
    val future = service.getFlatfyResponseFuture(1)
    Await.result(future, Duration.Inf)
    future.onComplete{case Success(value) => assert(value.status.isSuccess())}
  }
  
  test("countNumOfPages should return int num of pages") {
    val numOfPages = service.countNumOfPages()
    assert(numOfPages.isInstanceOf[Int])
    assert(numOfPages > 1000)
  }
}
