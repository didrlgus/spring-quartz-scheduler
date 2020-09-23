package com.quartzscheduler.config;

import com.quartzscheduler.scheduler.Best10ProductCachingJob;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Objects;

@RequiredArgsConstructor
@Configuration
public class QuartzConfig {

    //private final DataSource dataSource;

    // Job
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(Best10ProductCachingJob.class);

        return factory;
    }

    // cron trigger
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean(JobDetailFactoryBean jobDetailFactoryBean) {
        CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
        factory.setJobDetail(Objects.requireNonNull(jobDetailFactoryBean.getObject()));
        factory.setCronExpression("0 0/5 * * * ?");        // 5분 마다 한번씩 실행
        return factory;
    }

    // scheduler
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setTriggers(cronTriggerFactoryBean.getObject());
        //factory.setDataSource(dataSource);
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        return factory;
    }

}
