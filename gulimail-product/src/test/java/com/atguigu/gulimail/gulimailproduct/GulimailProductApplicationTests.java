package com.atguigu.gulimail.gulimailproduct;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimail.product.GulimailProductApplication;
import com.atguigu.gulimail.product.dao.AttrGroupDao;
import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.entity.SpuInfoEntity;
import com.atguigu.gulimail.product.service.BrandService;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.atguigu.gulimail.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimailProductApplication.class)
public class GulimailProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    OSSClient ossClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDaol;

    @Test
    public void testFile() throws FileNotFoundException {
//        String endpoint = "oss-cn-guangzhou.aliyuncs.com";
//// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tHRqzmtR9nxvQQCw1x6";
//        String accessKeySecret = "YMLcIfcDuLNZn43LuGAbiTaSAXCrTh";

// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("/Users/xzb/Downloads/nacos/bin/nacos.sh");
// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("gulimail-xzb", "bin", inputStream);

// 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成");
    }

    @Test
    public void contextLoads() {
        List<SpuItemAttrGroupVo> spuItemAttrGroupVoWithAttrsBySpuId = attrGroupDaol.getSpuItemAttrGroupVoWithAttrsBySpuId(47l, 225l);
        System.out.println(spuItemAttrGroupVoWithAttrsBySpuId);
    }


}
