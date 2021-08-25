package com.atguigu.gulimail.seaerch.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductSaveService {
    boolean productStatuseUp(List<SkuEsModel> esList) throws IOException;
}
