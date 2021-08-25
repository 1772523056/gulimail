package com.atguigu.common.constant;

import lombok.Data;

public class ProductConstant {

    public enum AttrEnum{
        BASE(1,"基本属性"),SALE(0,"销售属性");
        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    public enum SpuStatusEnum{
        NEW(0,"新建"),UP(1,"上架"),DOWN(2,"下架");
        private int code;
        private String msg;

        SpuStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
