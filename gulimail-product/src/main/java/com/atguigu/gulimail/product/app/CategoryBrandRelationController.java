package com.atguigu.gulimail.product.app;

import java.util.Arrays;

import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.vo.BrandVo;
import com.atguigu.gulimail.product.vo.CateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimail.product.service.CategoryBrandRelationService;
import com.atguigu.common.utils.R;



/**
 * Ʒ�Ʒ������
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

////    @RequestMapping("/brands/list")
////    public R
////    /**
//     * 列表
//     */
    @RequestMapping("/catelog/list")
    public R list(@RequestParam Long brandId){
        CateVo[] page = categoryBrandRelationService.queryCateList(brandId);

        return R.ok().put("data", page);
    }

    @RequestMapping("/brands/list")
    public R brandlist(@RequestParam Long catId){
        BrandEntity[] page = categoryBrandRelationService.queryBrandList(catId);
        BrandVo[] brandVo = new BrandVo[page.length];
        for (int i = 0; i < page.length; i++) {
            BrandVo brandVo1 = new BrandVo();
            brandVo1.setBrandId(page[i].getBrandId());
            brandVo1.setBrandName(page[i].getName());
            brandVo[i]=brandVo1;
        }
        return R.ok().put("data", brandVo);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveBrandRelation(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
