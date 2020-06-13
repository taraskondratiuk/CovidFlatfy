package com.example.job

import com.example.service.CovidCasesService
import com.example.utility.FileDownloader
import org.quartz.{Job, JobExecutionContext}

class DownloadAndRefreshCovidDataJob extends Job with FileDownloader {
  override def execute(context: JobExecutionContext): Unit = {
    val jobDataMap = context.getMergedJobDataMap
    downloadFile(jobDataMap.getString("uri"), jobDataMap.getString("path"))
    
    val covidCasesService = context.getScheduler.getContext.get("covidCasesService").asInstanceOf[CovidCasesService]
    covidCasesService.refreshMap(jobDataMap.getString("path"))
  }
}
