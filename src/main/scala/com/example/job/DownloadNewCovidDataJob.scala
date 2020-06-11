package com.example.job

import com.example.utility.FileDownloader
import org.quartz.{Job, JobExecutionContext}

class DownloadNewCovidDataJob extends Job with FileDownloader {
  override def execute(context: JobExecutionContext): Unit = {
    val jobDataMap = context.getMergedJobDataMap
    downloadFile(jobDataMap.getString("url"), jobDataMap.getString("path"))
  }
}
