package test.scala.com.example.job

import java.io.File

import main.scala.com.example.job.DownloadAndRefreshCovidDataJob
import main.scala.com.example.service.CovidCasesService
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.scalatest.FunSuite

class DownloadAndRefreshCovidDataJobTest extends FunSuite {
  test("job should download file") {
    val path = sys.env("PROJECT_PATH") + "\\src\\test\\resources\\jobtestdata.csv"
    
    new File(path).delete()
    val job = JobBuilder.newJob(classOf[DownloadAndRefreshCovidDataJob]).withIdentity("Job", "Group")
    val jobDataMap = new JobDataMap()
    jobDataMap.put("uri", sys.env("COVID_DATA_URI"))
    jobDataMap.put("path", path)
    job.setJobData(jobDataMap)
    
    val trigger: CronTrigger = TriggerBuilder.newTrigger()
      .withIdentity("Trigger", "Group")
      .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
      .forJob("Job", "Group")
      .build
    
    val scheduler = new StdSchedulerFactory().getScheduler
    
    val covidCasesService = CovidCasesService()
    scheduler.start()
    scheduler.getContext.put("covidCasesService", covidCasesService)
    scheduler.scheduleJob(job.build(), trigger)
    Thread.sleep(15000)
    assert(new File(path).exists())
    assert(covidCasesService.kyivCovidCasesMap.toSet.nonEmpty)
    
  }
}
