package com.atguigu.gulimail.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimail.product.service.AttrService;
import com.atguigu.gulimail.product.service.CategoryService;
import com.atguigu.gulimail.product.vo.AttrGroupWithAttrVo;
import com.atguigu.gulimail.product.vo.AttrRelationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * ���Է���
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
@RestController
@Slf4j
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @RequestMapping("{catelogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable long catelogId){
        AttrGroupWithAttrVo[] list=attrGroupService.getAttrGroupWithAttr(catelogId);
        return R.ok().put("data",list);
    }

    @RequestMapping("/attr/relation")
    public R upadteRelation(@RequestBody List<AttrAttrgroupRelationEntity> attrRelationVo){
        attrAttrgroupRelationService.saveBatch(attrRelationVo);
        return R.ok();

    }

    @RequestMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrRelationVo[] relation) {
        attrAttrgroupRelationService.deleteRelation(relation);
        return R.ok();
    }

    ///product/attrgroup/{attrgroupId}/noattr/relation
    @RequestMapping("/{attrgroupId}/noattr/relation")
    public R getNoRelation(@RequestParam Map<String, Object> params,
                           @PathVariable("attrgroupId") Long id) {
        PageUtils page = attrService.getNoRelation(id,params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {

        PageUtils page = attrGroupService.queryPage(params, catelogId);
        System.out.println(page);
        return R.ok().put("page", page);
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R getRelationAttr(@PathVariable("attrgroupId") Long id) {
        List<AttrEntity> attrEntity = attrService.getRelationAttr(id);
        return R.ok().put("data", attrEntity);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] categoryPath = categoryService.getCategoryPath(catelogId);
        attrGroup.setCatelogPath(categoryPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
