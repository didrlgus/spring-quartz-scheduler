package com.quartzscheduler.scheduler;

import com.shoppingmall.domain.product.Product;
import com.shoppingmall.domain.product.ProductRepository;
import com.shoppingmall.domain.productDisPrc.ProductDisPrc;
import com.shoppingmall.dto.ProductResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Best10ProductCachingJob implements Job {

    private final static String BEST10_PRODUCT_KEY = "redis-cache:best10ProductList";
    private final static String NEW8_PRODUCT_KEY = "redis-cache:new8ProductList";

    /**
     * 일정 주기마다 실행되는 메서드
     */
    @Override
    public void execute(JobExecutionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        List<ProductResponseDto.MainProductResponseDto> best10ProductList = getBest10ProductList(applicationContext);
        List<ProductResponseDto.MainProductResponseDto> new8ProductList = getNew8ProductList(applicationContext);

        setCaching(applicationContext, best10ProductList, new8ProductList);
    }

    public void setCaching(ApplicationContext applicationContext, Object bestProduct, Object newProduct) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(applicationContext);

        ValueOperations<String, Object> vop = redisTemplate.opsForValue();
        vop.set(BEST10_PRODUCT_KEY, bestProduct);
        vop.set(NEW8_PRODUCT_KEY, newProduct);
    }

    public List<ProductResponseDto.MainProductResponseDto> getBest10ProductList(ApplicationContext applicationContext) {
        ProductRepository productRepository = getProductRepository(applicationContext);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "purchaseCount"));

        List<Product> products = productRepository.findBestTop10Products(pageable);

        return getProductResponseDtoList(products);
    }

    public List<ProductResponseDto.MainProductResponseDto> getNew8ProductList(ApplicationContext applicationContext) {
        ProductRepository productRepository = getProductRepository(applicationContext);
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdDate"));

        List<Product> products = productRepository.findNewTop8Products(pageable);

        return getProductResponseDtoList(products);
    }

    public List<ProductResponseDto.MainProductResponseDto> getProductResponseDtoList(List<Product> products) {
        List<ProductResponseDto.MainProductResponseDto> productResponseDtoList = new ArrayList<>();

        for(Product product : products) {
            int disPrice = 0;
            if (product.getProductDisPrcList().size() > 0) {
                disPrice = getDisPrice(product);
            }
            productResponseDtoList.add(product.toMainProductResponseDto(disPrice));
        }

        return productResponseDtoList;
    }

    private int getDisPrice(Product product) {
        // 스트림 API를 사용하여 현재 날짜가 할인이 적용되는 날짜 사이에 있는 데이터 중 가장 할인률이 높은 데이터 하나만을 꺼내서 리스트로 저장
        // 현재 할인이 적용된 리스트 하나를 조회
        List<ProductDisPrc> disprcList
                = product.getProductDisPrcList().stream()
                .filter(productDisPrc -> {
                    LocalDateTime now = LocalDateTime.now();

                    return now.isAfter(productDisPrc.getStartDt())
                            && now.isBefore(productDisPrc.getEndDt());
                })
                .sorted().limit(1).collect(Collectors.toList());

        // 현재 할인이 적용된 리스트가 없을 수도 있으므로 if문 처리
        if (disprcList.size() > 0) {
            return disprcList.get(0).getDisPrc();
        }

        return 0;
    }

    public ProductRepository getProductRepository(ApplicationContext applicationContext) {
        return applicationContext.getBean("productRepository", ProductRepository.class);
    }

    public RedisTemplate<String, Object> getRedisTemplate(ApplicationContext applicationContext) {
        return applicationContext.getBean("redisQuartzTemplate", RedisTemplate.class);
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
