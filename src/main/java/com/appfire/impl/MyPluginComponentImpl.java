package com.appfire.impl;

import com.appfire.api.MyPluginComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.scheduler.*;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;

import static com.atlassian.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;

@ExportAsService({MyPluginComponent.class})
@Named("myPluginComponent")
public class MyPluginComponentImpl implements MyPluginComponent {
  @ComponentImport
  private final ApplicationProperties applicationProperties;
  @ComponentImport
  private final SchedulerService schedulerService;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  public MyPluginComponentImpl(final ApplicationProperties applicationProperties, SchedulerService schedulerService) {
    this.applicationProperties = applicationProperties;
    this.schedulerService = schedulerService;
    onStart(schedulerService);
  }

  public String getName() {
    if (null != applicationProperties) {
      return "myComponent:" + applicationProperties.getDisplayName();
    }

    return "myComponent";
  }

  private void onStart(SchedulerService schedulerService) {
    logger.error("Starting plugin");
    schedulerService.registerJobRunner(JobRunnerKey.of("foo"), new JobRunner() {
      @Nullable
      @Override
      public JobRunnerResponse runJob(JobRunnerRequest request) {
        return null;
      }
    });

    final JobConfig jobConfig = JobConfig.forJobRunnerKey(JobRunnerKey.of("foo"))
        .withSchedule(Schedule.forInterval(3600 * 1000, null))
        .withRunMode(RUN_ONCE_PER_CLUSTER)
        .withParameters(Collections.singletonMap("foo", "bar"));

    final JobConfig jobConfig2 = JobConfig.forJobRunnerKey(JobRunnerKey.of("foo"))
        .withSchedule(Schedule.forInterval(3600 * 1000, null))
        .withRunMode(RUN_ONCE_PER_CLUSTER)
        .withParameters(Collections.singletonMap("foo", Foobar.AAA));
    logger.error("Job config created");
    try {
      logger.error("Scheduling job");
      schedulerService.scheduleJob(JobId.of("bar"), jobConfig);
      schedulerService.scheduleJob(JobId.of("bar2"), jobConfig2);
      logger.error("Scheduled job");
    } catch (SchedulerServiceException e) {
      logger.error("Error", e);
      throw new RuntimeException(e);
    }
  }

  enum Foobar implements Serializable {
    AAA,
    BBB
  }
}
