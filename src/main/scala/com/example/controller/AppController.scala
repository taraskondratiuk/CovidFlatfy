package main.scala.com.example.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import main.scala.com.example.job.{DownloadAndRefreshCovidDataJob, SaveRealEstateWithCovidCasesListingsJob}
import main.scala.com.example.model.RealEstateWithCovidCasesProtocol._
import main.scala.com.example.repository.RealEstateWihtCovidCasesListingsRepository
import main.scala.com.example.service.{CovidCasesService, RealEstateService}
import main.scala.com.example.utility.{FileDownloader, JobScheduler}
import org.quartz.impl.StdSchedulerFactory
import spray.json._

import scala.concurrent.Future
import scala.io.StdIn

object AppController extends App with JobScheduler with FileDownloader {
  implicit val system = ActorSystem("covid-flatfy-system")
  implicit val executionContext = system.dispatcher
  
  downloadFile("https://covid19.gov.ua/csv/data.csv", "F:\\1PROGA\\stsala\\CovidFlatfy\\data\\data.csv")
  
  private val covidCasesService = CovidCasesService(sys.env("PROJECT_PATH" + sys.env("COVID_DATA_PATH")))
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
        complete(HttpEntity(ContentTypes.`application/json`, listingsRepo.getLastListing().toJson.compactPrint))
      }
    } ~
      path("today") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, listingsRepo.getThisDayListings().toJson.compactPrint))
        }
      } ~
  path("run") {
    post {
      Future(listingsRepo.saveListing(realEstateService.getTopTenRealEstateWithCovidCasesByPriceSqm(numPages = sys.env("NUM_PAGES").toInt)))
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "job started"))
    }
  }
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
