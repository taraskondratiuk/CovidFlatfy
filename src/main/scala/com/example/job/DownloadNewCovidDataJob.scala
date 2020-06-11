package com.example.job

import com.example.utility.FileDownloader
import org.quartz.{Job, JobExecutionContext}

class DownloadNewCovidDataJob extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    val fileDownloader = FileDownloader()
    val jobDataMap = context.getMergedJobDataMap
    fileDownloader.downloadFile(jobDataMap.getString("url"), jobDataMap.getString("path"))
  }
}
