package com.atguigu.gulimail.seaerch.service;

import com.atguigu.gulimail.seaerch.vo.SearchParam;
import com.atguigu.gulimail.seaerch.vo.SearchResult;
import org.springframework.stereotype.Service;

@Service
public interface MailSearchService {
    SearchResult listPage(SearchParam searchParam);
}
