package com.atguigu.gulimail.seaerch.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimail-product")
public interface AttrsFeignService {

    @RequestMapping("product/attr/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) ;
}
