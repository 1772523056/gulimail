package com.atguigu.gulimail.product.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import com.atguigu.gulimail.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate redis;

    @RequestMapping({"/", "/index.thml"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntityList = categoryService.getLevelOne();
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    @ResponseBody
    @GetMapping("index/json/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        RLock lock = redissonClient.getLock("mylocal");
        lock.lock();
        try {
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(20000);
        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/read")
    public String read() throws InterruptedException {
        RReadWriteLock lock = redissonClient.getReadWriteLock("l");
        RLock rLock = lock.readLock();
        String s = null;
        try {
            rLock.lock();
            System.out.println("加锁成功" + Thread.currentThread().getId());
            s = redis.opsForValue().get("aaa");

        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/write")
    public String write() throws InterruptedException {
        RReadWriteLock lock = redissonClient.getReadWriteLock("l");
        RLock rLock = lock.writeLock();
        try {
            rLock.lock();
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(20000);
            redis.opsForValue().set("aaa", UUID.randomUUID().toString());
        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            rLock.unlock();
        }
        return "ok";
    }
    @ResponseBody
    @GetMapping("/park")
    public String park(){
        RSemaphore door = redissonClient.getSemaphore("door");
        try {
            door.acquire();
        } catch (InterruptedException e) {

        }
        return "ok";
    }
    @ResponseBody
    @GetMapping("/go")
    public String go(){
        RSemaphore door = redissonClient.getSemaphore("door");
        try {
            door.release();
        } catch (Exception e) {

        }
        return "ok";
    }

}
