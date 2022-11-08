package com.gsg.wx;

import com.gsg.wx.service.IWxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WxApplicationTests {

    @Autowired
    IWxService wxService;

    @Test
    void test() {
//        wxService.sendWxSleep("msg0009");
    }

}
