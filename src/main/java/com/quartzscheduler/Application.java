package com.quartzscheduler;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling   // 스케줄러 사용
@SpringBootApplication
public class Application {

    private static final String APPLICATION_LOCATIONS = "spring.config.location="
            + "classpath:application-real.properties,"
            + "/home/ec2-user/app/config/springboot-webservice/real-quartz-application.yml";
            //+ "C:\\config\\real-quartz-application.yml";

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .properties(APPLICATION_LOCATIONS)
                .run(args);
    }
}

