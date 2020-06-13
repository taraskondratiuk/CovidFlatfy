package com.example.utility

import org.quartz._

trait JobScheduler {
  def scheduleJob(jobClass: Class[_ <: Job],
                  scheduler: Scheduler,
                  cronExpression: String,
                  jobName: String,
                  jobData: (String, String)*): TriggerKey = {
    
    val job = JobBuilder.newJob(jobClass).withIdentity(jobName, "Group")
    val jobDataMap = new JobDataMap()
    jobData.foreach(kv => jobDataMap.put(kv._1, kv._2))
    
    job.setJobData(jobDataMap)
    
    val trigger: CronTrigger = TriggerBuilder.newTrigger()
      .withIdentity("Trigger" + jobName, "Group")
      .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
      .forJob(jobName, "Group")
      .build
    
    scheduler.scheduleJob(job.build(), trigger)
    trigger.getKey
  }
}
