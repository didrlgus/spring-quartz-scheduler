package com.quartzscheduler.common;

public class SqlUtils {

    /**
     가장 인기있는 상품 10개를 뽑아내는 쿼리
     **/
    public final static String BEST10_PRODUCT_SQL
            = "select p.id as id, p.product_nm as product_nm, p.title_img as title_img, p.price as price, " +
            "p.rate_avg as rate_avg, max(pdp.dis_prc) as dis_prc, floor(p.price*(1-max(pdp.dis_prc)/100)) as sale_prc\n" +
            "  from product_dis_prc pdp\n" +
            "left outer join product p \n" +
            "    on pdp.product_id = p.id\n" +
            "group by p.id\n" +
            "order by p.purchase_count desc\n" +
            "limit 10";

}
