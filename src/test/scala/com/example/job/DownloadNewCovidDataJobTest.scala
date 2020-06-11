package com.example.job

import java.io.File

import org.quartz.impl.StdSchedulerFactory
import org.quartz._
import org.scalatest.FunSuite

class DownloadNewCovidDataJobTest extends FunSuite {
  test("job should download file") {
    val path = sys.env("PROJECT_PATH") + "\\src\\test\\jobtestdata.csv"
    
    new File(path).delete()
    val job = JobBuilder.newJob(classOf[DownloadNewCovidDataJob]).withIdentity("Job", "Group")
    val jobDataMap = new JobDataMap()
    jobDataMap.put("url", sys.env("COVID_DATA_URI"))
    jobDataMap.put("path", path)
    job.setJobData(jobDataMap)
  
    val trigger: CronTrigger = TriggerBuilder.newTrigger()
      .withIdentity("Trigger", "Group")
      .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
      .forJob("Job", "Group")
      .build
    val scheduler = new StdSchedulerFactory().getScheduler
    scheduler.start()
    scheduler.scheduleJob(job.build(), trigger)
    Thread.sleep(15000)
    assert(new File(path).exists())
  }
}
