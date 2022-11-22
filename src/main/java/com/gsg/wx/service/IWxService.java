package com.gsg.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gsg.wx.model.User;

/**
 * TODO
 *
 * @author shuaigang
 * @date 2022/8/26 12:38
 */
public interface IWxService extends IService<User> {

    String sendWxMsg();

    String sendWxRemind(String messageId, String round);

    String sendWxSleep(String messageId);

    String sendWxCustomMsg(String id, String message);
}
