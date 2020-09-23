package com.quartzscheduler.scheduler;

import com.quartzscheduler.common.CustomRowMapper;
import com.shoppingmall.dto.ProductResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Objects;

import static com.quartzscheduler.common.SqlUtils.BEST10_PRODUCT_SQL;

@Slf4j
public class Best10ProductCachingJob implements Job {

    private final static String BEST10_PRODUCT_KEY = "redis-cache:best10ProductList";

    @Override
    public void execute(JobExecutionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        List<ProductResponseDto.MainProductResponseDto> result = getBest10ProductList(applicationContext);

        setCaching(applicationContext, result);
    }

    public List<ProductResponseDto.MainProductResponseDto> getBest10ProductList(ApplicationContext applicationContext) {
        JdbcTemplate jdbcTemplate = getJdbcTemplagte(applicationContext);

        return jdbcTemplate.query(BEST10_PRODUCT_SQL, new CustomRowMapper());
    }

    public void setCaching(ApplicationContext applicationContext, Object value) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(applicationContext);

        ValueOperations<String, Object> vop = redisTemplate.opsForValue();

        vop.set(BEST10_PRODUCT_KEY, value);
    }

    public RedisTemplate<String, Object> getRedisTemplate(ApplicationContext applicationContext) {
        return applicationContext.getBean("redisQuartzTemplate", RedisTemplate.class);
    }

    public JdbcTemplate getJdbcTemplagte(ApplicationContext applicationContext) {
        return Objects.requireNonNull(applicationContext).getBean(JdbcTemplate.class);
    }

    public ApplicationContext getApplicationContext(JobExecutionContext context) {
        ApplicationContext applicationContext = null;

        try {
            applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
        } catch (Exception e) {
            log.error("com.quartzscheduler.scheduler.Best10ProductCachingJob execute() error {}", e.toString());
        }

        return applicationContext;
    }
}
