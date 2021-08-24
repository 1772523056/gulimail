package com.atguigu.gulimail.product.service.impl;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.dao.SpuInfoDao;
import com.atguigu.gulimail.product.entity.*;
import com.atguigu.gulimail.product.feign.CouponFeignService;
import com.atguigu.gulimail.product.service.*;
import com.atguigu.gulimail.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //    @Transactional
//    @Override
//    public void saveSpuInfo(SpuSaveVo spuInfo) {
//        //1.保存spu的基本信息
//        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
//        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
//        spuInfoEntity.setCreateTime(new Date());
//        spuInfoEntity.setUpdateTime(new Date());
//        this.save(spuInfoEntity);
//
//        //2.保存spu的描述图片
//        List<String> decript = spuInfo.getDecript();
//        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
//        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
//        spuInfoDescEntity.setDecript(String.join(",", decript));
//        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
//
//        //3.保存spu的图片集
//        List<String> images1 = spuInfo.getImages();
//        spuImagesService.saveImages(spuInfoEntity.getId(), images1);
//
//        //4.保存spu的规格参数
//        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
//        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(ele -> {
//            ProductAttrValueEntity v = new ProductAttrValueEntity();
//            v.setSpuId(spuInfoEntity.getId());
//            v.setAttrName(attrService.getById(ele.getAttrId()).getAttrName());
//            v.setAttrValue(ele.getAttrValues());
//            v.setQuickShow(ele.getShowDesc());
//            v.setAttrId(ele.getAttrId());
//            return v;
//        }).collect(Collectors.toList());
//        productAttrValueService.saveBatch(collect);
//        //5.保存spu对应的sku信息
//
//        List<Skus> skus = spuInfo.getSkus();
//        if (skus != null && skus.size() > 0) {
//            skus.forEach(ele -> {
//                String defaultImage = "";
//                for (Images image : ele.getImages()) {
//                    if (image.getDefaultImg() == 1)
//                        defaultImage = image.getImgUrl();
//                }
//                //5.1 SKU的基本信息；pms_sku_info
//                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
//                BeanUtils.copyProperties(ele, skuInfoEntity);
//                skuInfoEntity.setSpuId(spuInfoEntity.getId());
//                //skuInfoEntity.setSkuDesc();
//                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
//                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
//                skuInfoEntity.setSkuDefaultImg(defaultImage);
//                skuInfoEntity.setSaleCount(0L);
//
//                skuInfoService.saveSkuInfo(skuInfoEntity);
//
//                List<Images> images = ele.getImages();
//                List<SkuImagesEntity> skuImagesEntities = images.stream().map(img -> {
//                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
//                    BeanUtils.copyProperties(img, skuImagesEntity);
//                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
//
//                    return skuImagesEntity;
//                }).filter(entity -> {
//                    return StringUtils.isNotEmpty(entity.getImgUrl());
//                }).collect(Collectors.toList());
//
//                //5.3 SKU的销售属性信息：pms_sku_sale_attr_value
//                List<Attr> attrs = ele.getAttr();
//                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
//                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
//                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
//                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
//                    return skuSaleAttrValueEntity;
//                }).collect(Collectors.toList());
//
//                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
//
//                //5.4 SKU的优惠，满减等信息；sms_sku_ladder；sms_sku_full_reduction；sms_member_price
//                SkuReductionTo skuReductionTo = new SkuReductionTo();
//                BeanUtils.copyProperties(item, skuReductionTo);
//                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
//
//                if (skuReductionTo.getFullCount() <= 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
//                    R r1 = couponFenService.saveSkuReduction(skuReductionTo);
//                    if (r1.getCode() != 0) {
//                        log.error("保存远程SKU优惠信息失败");
//                    }
//                }
//
//
//            });
//        }
//    }
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1. 保存spu基本信息；pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());

        this.save(spuInfoEntity);
        //2. 保存SPU的描述图片；pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();

        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));

        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3. 保存SPU的图片集；pms_spu_images
        List<String> spuSaveVoImages = spuSaveVo.getImages();

        spuImagesService.saveImages(spuInfoEntity.getId(), spuSaveVoImages);

        //4. 保存SPU的规格参数，pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
            attrValueEntity.setSpuId(spuInfoEntity.getId());
            attrValueEntity.setAttrId(item.getAttrId());
            attrValueEntity.setAttrName(attrService.getById(item.getAttrId()).getAttrName());
            attrValueEntity.setAttrValue(item.getAttrValues());
            attrValueEntity.setQuickShow(item.getShowDesc());

            return attrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveBatch(productAttrValueEntities);


        //5.0 保存SPU的积分信息；sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());

        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("保存远程SPU积分信息失败");
        }

        //5. 保存SPU对应的所有SKU信息
        List<Skus> skus = spuSaveVo.getSkus();

        if (skus != null && skus.size() > 0) {


            skus.forEach(item -> {
                //在每个SKU中众多images中，只有一个是默认图片，当为默认图片时，default_img=1
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                //5.1 SKU的基本信息；pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                //skuInfoEntity.setSkuDesc();
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoEntity.setSaleCount(0L);

                skuInfoService.saveSkuInfo(skuInfoEntity);

                //5.2 SKU的图片信息；pms_spu_images
                List<Images> images = item.getImages();
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(img, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());

                    return skuImagesEntity;
                }).filter(ele -> !StringUtils.isEmpty(ele.getImgUrl())).collect(Collectors.toList());

                skuImagesService.saveBatch(skuImagesEntities);


                //5.3 SKU的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4 SKU的优惠，满减等信息；sms_sku_ladder；sms_sku_full_reduction；sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());

                if (skuReductionTo.getFullCount() <= 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("保存远程SKU优惠信息失败");
                    }
                }


            });

        }

    }


}