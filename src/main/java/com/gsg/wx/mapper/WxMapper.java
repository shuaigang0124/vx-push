package com.gsg.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsg.wx.VO.TempVO;
import com.gsg.wx.VO.TextVO;
import com.gsg.wx.model.Message;
import com.gsg.wx.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * TODO
 *
 * @author shuaigang
 * @date 2022/8/26 12:37
 */
@Mapper
public interface WxMapper extends BaseMapper<User> {

    List<User> getAllUser();

    List<TempVO> getTemp(String tempId);

    Message getMessage(String id);

    Integer findChpByText(String text);

    Integer findDuByText(String text);

    Integer findSleepByText(String text);

    void insertChpText(String text);

    void insertDuText(String text);

    void insertSleepText(String text);
}
