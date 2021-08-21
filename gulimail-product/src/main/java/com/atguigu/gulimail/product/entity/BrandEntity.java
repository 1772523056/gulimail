package com.atguigu.gulimail.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * Ʒ��
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *品牌ID
     */
    @NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
    @Null(message = "添加不能指定品牌id", groups = {AddGroup.class})
    @TableId
    private Long brandId;

    @NotBlank(message = "品牌名必须提交", groups = { AddGroup.class})
    private String name;

    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {UpdateGroup.class, AddGroup.class})
    private String logo;

    private String descript;

    @NotNull(groups = {AddGroup.class})
    @ListValue(Values = {1, 0}, groups = {UpdateGroup.class, AddGroup.class})
    private Integer showStatus;

    @NotNull(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {UpdateGroup.class, AddGroup.class})
    private String firstLetter;

    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须是一个大于零的数字", groups = {UpdateGroup.class, AddGroup.class})
    private Integer sort;

}
