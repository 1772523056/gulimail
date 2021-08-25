package com.atguigu.gulimail.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimail-ware")
public interface WareFeignService {
    @PostMapping("ware/waresku/hasstock")
     R<List<SkuHasStockVo>> hasStock(@RequestBody List<Long> skuIds);

}
