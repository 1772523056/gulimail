package com.atguigu.gulimail.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimail.product.controller")
public class GulimailExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{},异常类型{}", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        if (result.hasErrors()) {
            HashMap<String, String> map = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                String defaultMessage = error.getDefaultMessage();
                String field = error.getField();
                map.put(field, defaultMessage);
            }
            return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMesaage()).put("data", map);
        }
        return R.error();
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error("数据校验出现问题{},异常类型{}", e.getMessage(), e.getClass());
        e.printStackTrace();
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMesaage());
    }
}
