package main.scala.com.example.job

import main.scala.com.example.repository.RealEstateWihtCovidCasesListingsRepository
import main.scala.com.example.service.RealEstateService
import org.quartz.{Job, JobExecutionContext}

class SaveRealEstateWithCovidCasesListingsJob extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    val realEstateService = context.getScheduler.getContext.get("realEstateService").asInstanceOf[RealEstateService]
    val repo = context.getScheduler.getContext.get("listingsRepo").asInstanceOf[RealEstateWihtCovidCasesListingsRepository]
    
    repo.saveListing(realEstateService.getTopTenRealEstateWithCovidCasesByPriceSqm(
      sys.env("NUM_PAGES").toInt)) //fixme change to default num of pages if long request processing time fixed
  }
}
