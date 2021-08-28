package com.atguigu.gulimail.seaerch.controller;

import com.atguigu.gulimail.seaerch.service.MailSearchService;
import com.atguigu.gulimail.seaerch.vo.SearchParam;
import com.atguigu.gulimail.seaerch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class SearchController {
@Autowired
    MailSearchService mailSearchService;
    @GetMapping("list.html")
    public String list(SearchParam searchParam){
        SearchResult searchResult=mailSearchService.listPage(searchParam);
        return "list";

    }
}
