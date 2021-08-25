package com.atguigu.gulimail.ware.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.ware.vo.SkuHasStockVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.ware.dao.WareSkuDao;
import com.atguigu.gulimail.ware.entity.WareSkuEntity;
import com.atguigu.gulimail.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {
        return skuIds.stream().map(ele -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            long num = baseMapper.getSkuStock(ele);
            skuHasStockVo.setHasStock(num > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

//    @Override
//    public void addStock(Long skuId, Long wareId, Integer skuNum) {
//
//        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
//
//        if(wareSkuEntities == null || wareSkuEntities.size() ==0 ){
//            //新增
//            WareSkuEntity wareSkuEntity = new WareSkuEntity();
//            wareSkuEntity.setSkuId(skuId);
//            wareSkuEntity.setWareId(wareId);
//            wareSkuEntity.setStock(skuNum);
//            wareSkuEntity.setStockLocked(0);
//
//            //远程查询SKU的name，若失败无需回滚
//            try {
//                R info = productFeignService.info(skuId);
//                if(info.getCode() == 0){
//                    Map<String,Object> data=(Map<String,Object>)info.get("skuInfo");
//                    wareSkuEntity.setSkuName((String) data.get("skuName"));
//                }
//            } catch (Exception e) {
//
//            }
//
//            wareSkuDao.insert(wareSkuEntity);
//        }else{
//            //插入
//            wareSkuDao.addStock(skuId,wareId,skuNum);
//        }
//
//    }

}