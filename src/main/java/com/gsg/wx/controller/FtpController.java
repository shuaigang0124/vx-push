package com.gsg.wx.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author shuaigang
 * @date 2022/9/14 9:45
 */
@RestController
@RequestMapping("/ftp")
public class FtpController {

    @Scheduled(cron = "0 0 0 ? * *")
    @RequestMapping("/readFtpFile")
    public String readFtpFile() {

        return "登录信息数据采集成功";
    }
}
