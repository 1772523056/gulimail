package com.atguigu.gulimail.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimail.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private Map<String, Map<String, List<Catalog2Vo>>> map1 = new HashMap<>();

    @Autowired
    StringRedisTemplate redis;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        return categoryEntities.stream()
                .filter(ele -> ele.getParentCid() == 0)
                .map(ele -> {
                    ele.setChildren(getChildrens(ele, categoryEntities));
                    return ele;
                })
                .sorted(Comparator.comparingInt(num -> (num.getSort() == null ? 0 : num.getSort())))
                .collect(Collectors.toList());

    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(ele -> ele.getParentCid() == root.getCatId())
                .map(ele -> {
                    ele.setChildren(getChildrens(ele, all));
                    return ele;
                })
                .sorted(Comparator.comparingInt(num -> (num.getSort() == null ? 0 : num.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 判断是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] getCategoryPath(Long catelogId) {
        ArrayList<Long> list = new ArrayList<>();
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid != 0) {
            getPath(parentCid, list);
        }
        list.add(catelogId);
        return list.toArray(new Long[0]);

    }

    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public List<CategoryEntity> getLevelOne() {

        System.out.println("getLevelOne");
        List<CategoryEntity> cat_level = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return cat_level;
    }

    //TODO 加入redis缓存

    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        String catalogJson = redis.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //todo 可能有很多线程同时进来查询
            System.out.println("开始查数据库-------");
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLockV2();
            return catalogJsonFromDb;
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
    }
    //todo 版本1:
//todo 很多线程同时进来查询时,用分布式锁确保只有一个线程会访问数据库

    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLockV1() {

        //todo 核心1:加锁和设置时间的结合,保证原子性,确保会正确过期
        String uuid = UUID.randomUUID().toString();
        Boolean aBoolean = redis.opsForValue().setIfAbsent("k", uuid, 300, TimeUnit.SECONDS);
        if (aBoolean) {
            System.out.println("获取分布式锁成功");
            Map<String, List<Catalog2Vo>> dataFromDb = null;
            //todo 确保业务执行完之前key不会自动过期
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //todo 核心2:解锁时确定要删除的key是自己key,保证原子性,确保不会删除别人的key
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redis.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList("k"), uuid);
                return dataFromDb;
            }

        } else {
            System.out.println("获取分布式锁失败");
            return getCatalogJsonFromDbWithRedisLockV1();
        }

    }
    //todo V2 redisson

    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLockV2() {
        RLock catalog = redissonClient.getLock("catalog");
        catalog.lock();
        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            catalog.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2Vo>> getDataFromDb() {
//
        String catalogJson = redis.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        System.out.println("查了数据库------");
        HashMap<String, List<Catalog2Vo>> map = new HashMap<>();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> levelOne = getParent_cid(0l, categoryEntities);

        for (CategoryEntity level1 : levelOne) {
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            map.put(level1.getCatId().toString(), catalog2Vos);

            List<CategoryEntity> levelTwo = getParent_cid(level1.getCatId(), categoryEntities);
            for (CategoryEntity level2 : levelTwo) {
                List<Catalog2Vo.Catalog3Vo> catalog3Vos = new ArrayList<>();
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                catalog2Vo.setCatalog1Id(level1.getCatId().toString());
                catalog2Vo.setCatalog3List(catalog3Vos);
                catalog2Vo.setId(level2.getCatId().toString());
                catalog2Vo.setName(level2.getName());

                List<CategoryEntity> levelThree = getParent_cid(level2.getCatId(), categoryEntities);
                for (CategoryEntity level3 : levelThree) {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                    catalog3Vo.setCatalog2Id(level2.getCatId().toString());
                    catalog3Vo.setName(level3.getName());
                    catalog3Vo.setId(level3.getCatId().toString());
                    catalog3Vos.add(catalog3Vo);
                }

                catalog2Vos.add(catalog2Vo);
            }
        }
        redis.opsForValue().set("catalogJson", JSON.toJSONString(map));
        return map;
    }


    private List<CategoryEntity> getParent_cid(Long id, List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().filter(ele -> ele.getParentCid() == id).collect(Collectors.toList());
    }

    private void getPath(Long id, ArrayList<Long> list) {
        CategoryEntity categoryEntity = baseMapper.selectById(id);
        if (categoryEntity.getParentCid() != 0) {
            getPath(categoryEntity.getParentCid(), list);
        }
        list.add(id);
    }
}