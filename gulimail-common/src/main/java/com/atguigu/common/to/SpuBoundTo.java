package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundTo {
    private Long SpuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
