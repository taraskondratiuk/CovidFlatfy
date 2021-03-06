package com.example.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.example.job.{DownloadAndRefreshCovidDataJob, SaveRealEstateWithCovidCasesListingsJob}
import com.example.repository.RealEstateWihtCovidCasesListingsRepository
import com.example.service.{CovidCasesService, RealEstateService}
import com.example.utility.{FileDownloader, JobScheduler}
import org.quartz.impl.StdSchedulerFactory
import com.example.model.RealEstateWithCovidCasesProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.Materializer

import scala.concurrent.Future
import spray.json.DefaultJsonProtocol._

object AppController extends App with JobScheduler with FileDownloader {
  implicit val system = ActorSystem("covid-flatfy-system")
  implicit val executionContext = system.dispatcher
  
  downloadFile(sys.env("COVID_DATA_URI"), sys.env("PROJECT_PATH") + sys.env("COVID_DATA_PATH"))
  
  private val covidCasesService = CovidCasesService(sys.env("PROJECT_PATH") + sys.env("COVID_DATA_PATH"))
  private val realEstateService = RealEstateService(covidCasesService.kyivCovidCasesMap)
  private val listingsRepo = RealEstateWihtCovidCasesListingsRepository(sys.env("DB_HOST"), sys.env("DB_PORT").toInt)
  private val jobScheduler = new StdSchedulerFactory().getScheduler
  jobScheduler.getContext.put("covidCasesService", covidCasesService)
  jobScheduler.getContext.put("realEstateService", realEstateService)
  jobScheduler.getContext.put("listingsRepo", listingsRepo)
  jobScheduler.start()
  
  //refreshing covid cases data every day at 4am utc
  scheduleJob(classOf[DownloadAndRefreshCovidDataJob], jobScheduler, "0 0 4 ? * * *", "covidDataDownload",
    "uri" -> sys.env("COVID_DATA_URI"), "path" -> (sys.env("PROJECT_PATH") + sys.env("COVID_DATA_PATH")))
  
  //adding real estate with covid cases listings every hour
  scheduleJob(classOf[SaveRealEstateWithCovidCasesListingsJob], jobScheduler, "0 0 * ? * *",
    "realEstateListingsSave")
  
  val route =
    path("last") {
      get {
        complete {
          ToResponseMarshallable {
            listingsRepo.getLastListing()
          }
        }
      }
    } ~
//      path("today") {
//        get {
//          complete {
//            ToResponseMarshallable {
//              listingsRepo.getThisDayListings()
//            }
//          }
//        }
//      } ~
  path("run") {
    post {
      Future(listingsRepo.saveListing(realEstateService.getTopTenRealEstateWithCovidCasesByPriceSqm(numPages = sys.env("NUM_PAGES").toInt)))
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "job started"))
    }
  }
  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
  
  println(s"Server online at http://127.0.0.1:8080/")
}
