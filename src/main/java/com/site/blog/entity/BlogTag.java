package com.site.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 标签表
 * </p>
 *
 * @author: Cifor
 * @since 2019-08-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BlogTag implements Serializable {

private static final long serialVersionUID=1L;

    /**
     * 标签表主键id
     */
    @NotBlank(message = "标签id不能为空")
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer tagId;

    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 是否删除 0=否 1=是
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;


}
