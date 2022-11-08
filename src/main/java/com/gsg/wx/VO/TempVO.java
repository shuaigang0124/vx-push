package com.gsg.wx.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TODO
 *
 * @author shuaigang
 * @date 2022/8/26 13:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TempVO implements Serializable {

    private static final long serialVersionUID = 5903340942757324446L;

    private String id;

    private String wxNumber;

    private String cityNum;

    private String birthday;

    private String togetherDate;

    private String message;

    private String template;

}
