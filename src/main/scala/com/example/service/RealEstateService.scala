package com.example.service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Source}
import com.example.dto.RealEstate._
import com.example.dto.RealEstateWithCovidCases
import spray.json.{JsNumber, JsObject, _}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RealEstateService(val covidCasesMap: Map[String, (Int, Set[String])]) {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  val seqRealEstateToRealEstateWithCovidCasesFlow
  : Flow[Seq[RealEstate], Option[RealEstateWithCovidCases], NotUsed] =
    Flow[Seq[RealEstate]]
      .async
      .mapConcat(identity)
      .map { realEstate =>
        val street = realEstate.geo.address.street
          .flatMap(strt => Option(strt.name.toLowerCase.replace(".,'\"`?!", "")))
        if (street.isDefined) {
          val covidCasesForStreet = covidCasesMap.get(street.get)
          if (covidCasesForStreet.isDefined) {
            Option(RealEstateWithCovidCases(
              street.get,
              realEstate.priceSqm,
              realEstate.updateTime,
              realEstate.singleRealtyUrl,
              covidCasesForStreet.get._1,
              covidCasesForStreet.get._2)
            )
          }
        }
        Option.empty
      }
    .filter(_.isDefined)
    
  
  
  def getRealEstateRequestsSource(pages: Int): Source[HttpRequest, NotUsed] = Source.
    fromIterator(() =>
      Range(1, pages + 1).map(i => flatfyRequest(i)).iterator
    )
  
  def parseRequestIntoSeqOfRealEstate(req: HttpRequest): Future[Seq[RealEstate]] =
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
      "page" -> JsNumber(page)
    )
  )
}

object RealEstateService {
  def apply(covidCasesMap: Map[String, (Int, Set[String])]): RealEstateService = new RealEstateService(covidCasesMap)
}