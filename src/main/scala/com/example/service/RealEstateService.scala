package com.example.service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.example.dto.RealEstate._
import com.example.dto.RealEstateWithCovidCases
import spray.json.{JsNumber, JsObject, _}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RealEstateService(val covidCasesMap: Map[String, (Int, Set[String])]) {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  val seqRealEstateToRealEstateWithCovidCasesFlow
  : Flow[Seq[RealEstate], RealEstateWithCovidCases, NotUsed] =
    Flow[Seq[RealEstate]]
      .async
      .mapConcat(identity)
      .filter(realEstate =>
        realEstate.geo.isDefined &&
          realEstate.geo.get.address.isDefined &&
          realEstate.geo.get.address.get.street.isDefined &&
          realEstate.geo.get.address.get.street.get.name.isDefined &&
          realEstate.updateTime.isDefined &&
          realEstate.singleRealtyUrl.isDefined &&
          realEstate.priceSqm.isDefined
      )
      .map { realEstate =>
        val street = realEstate.geo.get.address.get.street.get.name.get.toLowerCase.replace(".,'\"`?!", "")
        
        val covidCasesForStreet = covidCasesMap.get(street)
        if (covidCasesForStreet.isDefined) {
          Option(RealEstateWithCovidCases(
            street,
            realEstate.priceSqm.get,
            realEstate.updateTime.get,
            realEstate.singleRealtyUrl.get,
            covidCasesForStreet.get._1,
            covidCasesForStreet.get._2)
          )
        }
        Option.empty
      }
      .filter(_.isDefined)
      .map(_.get)
  
  def getTopTenRealEstateWithCovidCasesByPriceSqm: Seq[RealEstateWithCovidCases] = {
    val seqFuture = getRealEstateRequestsSource(countNumOfPages())
      .mapAsyncUnordered(1)(parseRequestIntoSeqOfRealEstate) //fixme exception if parallelism > 1
      .via(seqRealEstateToRealEstateWithCovidCasesFlow)
      .runWith(Sink.seq[RealEstateWithCovidCases])
    
    val sortedSeqFuture = seqFuture.map(seq => seq.sortBy(_.priceSqm).take(10))
    
    Await.result(sortedSeqFuture, Duration.Inf)
    
    sortedSeqFuture.value.get.get
  }
  
  def getRealEstateRequestsSource(pages: Int): Source[HttpRequest, NotUsed] = Source.
    fromIterator(() =>
      Range(1, pages + 1).map(i => flatfyRequest(i)).iterator
    )
  
  def parseRequestIntoSeqOfRealEstate(req: HttpRequest): Future[Seq[RealEstate]] = {
    Http()
      .singleRequest(req)
      .flatMap(response =>
        Unmarshal(response.entity).to[String]
          .map { responseString =>
            responseString
              .parseJson
              .asJsObject
              .getFields("data")
              .head
              .asJsObject
              .getFields("items")
              .head
              .convertTo[Seq[RealEstate]]
          }
      )
  }
  
  def countNumOfPages(): Int = {
    val responseFuture = getFlatfyResponseFuture(1)
    val numOfPagesFuture = responseFuture.flatMap {
      (value) => {
        Unmarshal(value.entity).to[String]
          .map {
            (value) =>
              value
                .parseJson
                .asJsObject
                .getFields("data")
                .head
                .asJsObject()
                .getFields("totalItems", "itemsPerPage")
              match {
                case Seq(JsNumber(totalItems), JsNumber(itemsPerPage)) => totalItems.toInt / itemsPerPage.toInt + 1
              }
          }
      }
      //todo add logging for failure
    }
    Await.result(numOfPagesFuture, Duration.Inf)
  }
  
  def getFlatfyResponseFuture(page: Int): Future[HttpResponse] = {
    Http().singleRequest(flatfyRequest(page))
  }
  
  private def flatfyRequest(page: Int) = HttpRequest(
    method = HttpMethods.POST,
    uri = sys.env("FLATFY_URI"),
    entity = HttpEntity(ContentTypes.`application/json`, flatfyRequestBody(page).compactPrint)
  )
  
  private def flatfyRequestBody(page: Int): JsObject = JsObject(
    "searchParams" -> JsObject(
      "city" -> JsNumber(1),
      "page" -> JsNumber(page),
      "realtyType" -> JsNumber(1),
      "contractType" -> JsNumber(1)
    )
  )
}

object RealEstateService {
  def apply(covidCasesMap: Map[String, (Int, Set[String])]): RealEstateService = new RealEstateService(covidCasesMap)
}
