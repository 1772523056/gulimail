package com.atguigu.gulimail.product.service.impl;

import com.alibaba.cloud.context.utils.StringUtils;
import com.atguigu.gulimail.product.entity.SkuImagesEntity;
import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;
import com.atguigu.gulimail.product.service.SkuImagesService;
import com.atguigu.gulimail.product.service.SpuInfoDescService;
import com.atguigu.gulimail.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.atguigu.gulimail.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.SkuInfoDao;
import com.atguigu.gulimail.product.entity.SkuInfoEntity;
import com.atguigu.gulimail.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(item -> {
                item.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }


        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }


        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max)) {

            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params), queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //skuinfo
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.baseMapper.selectById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);
        //spu的介绍
        CompletableFuture<Void> voidCompletableFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getBaseMapper().selectById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);
        //获取销售属性组合
        CompletableFuture<Void> voidCompletableFuture1 = infoFuture.thenAcceptAsync((res) -> {
            Long spuId = res.getSpuId();
            List<SkuItemSaleAttrVo> list = this.baseMapper.selectSkuAttrsBySpuId(spuId);
            skuItemVo.setSaleAttr(list);
        }, executor);
        //spu的规格参数信息
        CompletableFuture<Void> voidCompletableFuture2 = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> list1 = attrGroupService.getSpuItemAttrGroupVoWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(list1);
        });

        //skuimag
        CompletableFuture<Void> sku_id = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntityList = skuImagesService.getBaseMapper().selectList(
                    new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(skuImagesEntityList);
        });

        try {
            CompletableFuture.allOf(voidCompletableFuture,voidCompletableFuture1,voidCompletableFuture2,sku_id).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return skuItemVo;
    }


}