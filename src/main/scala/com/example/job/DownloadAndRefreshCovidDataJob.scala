package main.scala.com.example.job

import main.scala.com.example.service.CovidCasesService
import main.scala.com.example.utility.FileDownloader
import org.quartz.{Job, JobExecutionContext}

class DownloadAndRefreshCovidDataJob extends Job with FileDownloader {
  override def execute(context: JobExecutionContext): Unit = {
    val jobDataMap = context.getMergedJobDataMap
    downloadFile(jobDataMap.getString("uri"), jobDataMap.getString("path"))
    
    val covidCasesService = context.getScheduler.getContext.get("covidCasesService").asInstanceOf[CovidCasesService]
    covidCasesService.refreshMap(jobDataMap.getString("path"))
  }
}
