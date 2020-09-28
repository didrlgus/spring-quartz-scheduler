# quartz 스케줄러

### 시스템 구조
<img width="634" alt="structure" src="https://user-images.githubusercontent.com/40568894/94233289-9fac1200-ff42-11ea-9f96-521867668041.PNG">

## 사용 기술
* spring-boot 2.x
* spring-quartz
* spring-data-jpa
* redisTemplate

### 역할
* 5분마다 mysql의 best10, new8 상품 원본 데이터를 조회하여 레디스에 캐싱된 데이터 업데이트

