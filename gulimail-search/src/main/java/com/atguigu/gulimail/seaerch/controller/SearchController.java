package com.atguigu.gulimail.seaerch.controller;

import com.atguigu.gulimail.seaerch.service.MailSearchService;
import com.atguigu.gulimail.seaerch.vo.SearchParam;
import com.atguigu.gulimail.seaerch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
@Autowired
    MailSearchService mailSearchService;
    @GetMapping("list.html")
    public String list(SearchParam searchParam, Model model, HttpServletRequest req){
        String queryString = req.getQueryString();
        searchParam.set_queryString(queryString);
        SearchResult searchResult=mailSearchService.listPage(searchParam);
        model.addAttribute("result",searchResult);
        return "list";

    }
}
