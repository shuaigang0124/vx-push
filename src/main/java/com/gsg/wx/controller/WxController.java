package com.gsg.wx.controller;

import com.gsg.wx.service.IWxService;
import com.gsg.wx.utils.SftpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shuaigang
 * @date 2022/8/8 11:08
 */
@RestController
@RequestMapping("/wx")
public class WxController {

    @Autowired
    IWxService wxService;

    /**
     * 恋爱小助手 每天早上8：00执行推送
     */
    @Scheduled(cron = "0 0 8 ? * *")
    @RequestMapping("/getMorning")
    public String getMorning() {
        return wxService.sendWxMsg();
    }

    /**
     * 喝水提醒小助手第一轮提醒  每天早上9：30执行推送
     * 以下几轮每一小时后提醒
     */
    @Scheduled(cron = "0 30 9 ? * *")
    @RequestMapping("/getRemindOne")
    public String getRemindOne() {
        return wxService.sendWxRemind("msg0001", "这是今天的第一轮提醒");
    }

    @Scheduled(cron = "0 30 10 ? * *")
    @RequestMapping("/getRemindTwo")
    public String getRemindTwo() {
        return wxService.sendWxRemind("msg0002", "这是今天的第二轮提醒");
    }

    @Scheduled(cron = "0 30 11 ? * *")
    @RequestMapping("/getRemindThree")
    public String getRemindThree() {
        return wxService.sendWxRemind("msg0003", "这是今天的第三轮提醒");
    }

    @Scheduled(cron = "0 30 14 ? * *")
    @RequestMapping("/getRemindFour")
    public String getRemindFour() {
        return wxService.sendWxRemind("msg0004", "这是今天的第四轮提醒");
    }

    @Scheduled(cron = "0 30 15 ? * *")
    @RequestMapping("/getRemindFive")
    public String getRemindFive() {
        return wxService.sendWxRemind("msg0005", "这是今天的第五轮提醒");
    }

    @Scheduled(cron = "0 30 16 ? * *")
    @RequestMapping("/getRemindSix")
    public String getRemindSix() {
        return wxService.sendWxRemind("msg0006", "这是今天的第六轮提醒");
    }

    @Scheduled(cron = "0 30 17 ? * *")
    @RequestMapping("/getRemindSeven")
    public String getRemindSeven() {
        return wxService.sendWxRemind("msg0007", "这是今天的第七轮提醒");
    }

    @Scheduled(cron = "0 0 19 ? * *")
    @RequestMapping("/getRemindEight")
    public String getRemindEight() {
        return wxService.sendWxRemind("msg0008", "这是今天的第八轮提醒");
    }

    @Scheduled(cron = "0 0 23 ? * *")
    @RequestMapping("/getSleep")
    public String getSleep() {
        return wxService.sendWxSleep("msg0009");
    }

    //    @Scheduled(cron = "0 0 8 ? * *")
    @GetMapping(value = "/sendMsg/{id}/{message}")
    public String sendMsg(@PathVariable String id,@PathVariable String message) {
        return wxService.sendWxCustomMsg(id, message);
    }

    @GetMapping(value = "/delFtpFile/{deleteFile}")
    public String delFtpFile(@PathVariable String deleteFile) {
        SftpUtils sftp = null;
        try {
            sftp = new SftpUtils("175.178.9.64", 22, "root", "ShuaiGang19980510...");
            sftp.ftpLogin();
            sftp.deleteSFTP("/shuaigang/frontEnd/", deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert sftp != null;
            sftp.ftpLogOut();
        }
        return "success";
    }
}
