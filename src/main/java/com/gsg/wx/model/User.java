package com.gsg.wx.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TODO
 *
 * @author shuaigang
 * @date 2022/8/26 12:42
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("name")
    private String name;

    @TableField("wx_number")
    private Integer wxNumber;

    @TableField("city_num")
    private Integer cityNum;

    @TableField("birthday")
    private Integer birthday;

    @TableField("together_date")
    private Integer togetherDate;

    @TableField("message")
    private Integer message;

    /**
     * 是否启用,0-禁用,1-启用,默认值1
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 创建时间
     */
    @TableField("gmt_create")
    private LocalDateTime gmtCreate;


}
