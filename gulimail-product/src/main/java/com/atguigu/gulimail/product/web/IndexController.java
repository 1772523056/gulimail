package com.atguigu.gulimail.product.web;

import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import com.atguigu.gulimail.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

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
    public String hello() {
//        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return "hello";
    }
}
