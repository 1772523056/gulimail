package com.atguigu.gulimail.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.dao.SpuInfoDao;
import com.atguigu.gulimail.product.entity.*;
import com.atguigu.gulimail.product.feign.CouponFeignService;
import com.atguigu.gulimail.product.feign.SearchFeignService;
import com.atguigu.gulimail.product.feign.WareFeignService;
import com.atguigu.gulimail.product.service.*;
import com.atguigu.gulimail.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
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
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

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

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(item -> {
                item.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && (!"0".equalsIgnoreCase(brandId))) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && (!"0".equalsIgnoreCase(catelogId))) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<SkuInfoEntity> skuInfoList = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        //TODO 查询当前spu的所有可以被用来检索规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(
                new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        List<Long> attrIdList = productAttrValueEntities.stream()
                .map(e -> e.getAttrId()).collect(Collectors.toList());

        List<Long> list = attrService.selectSearchAttrs(attrIdList);
        Set<Long> idSet = new HashSet<>(list);

        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream()
                .filter(ele -> idSet.contains(ele.getAttrId()))
                .map(e -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(e, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());

        List<Long> skuIds = skuInfoList.stream().map(ele -> ele.getSkuId()).collect(Collectors.toList());
        //远程调用若有异常则继续
        Map<Long, Boolean> collect = null;
        try {
            R<List<SkuHasStockVo>> skuHasStockVos = wareFeignService.hasStock(skuIds);
            collect = skuHasStockVos.getData().stream()
                    .collect(Collectors.toMap(e -> e.getSkuId(), ele -> ele.isHasStock()));
        } catch (Exception e) {
            log.error("{}",e);
        }


        Map<Long, Boolean> finalCollect = collect;
        List<SkuEsModel> skuEsModelList = skuInfoList.stream().map(ele -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(ele, esModel);
            //skuImg,skuPrice
            esModel.setSkuImag(ele.getSkuDefaultImg());
            esModel.setSkuPrice(ele.getPrice());
            //hasStock,hotScore
            //TODO 远程调用查询库存系统是否有库存
            if (finalCollect == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalCollect.get(esModel.getSkuId()));
            }
            //TODO 热度评分 默认0
            esModel.setHotScore(0l);

            //TODO 查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getBaseMapper().selectById(ele.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            esModel.setCatalogName(categoryService.getBaseMapper().selectById(ele.getCatalogId()).getName());
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());

        //最终把数据发给es保存
        R r = searchFeignService.productStatuseUp(skuEsModelList);
        if(r.getCode()==0){
            baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatusEnum.UP.getCode());
        }
    }


}