package com.gsg.wx.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gsg.wx.VO.TempVO;
import com.gsg.wx.mapper.WxMapper;
import com.gsg.wx.model.Message;
import com.gsg.wx.model.User;
import com.gsg.wx.service.IWxService;
import com.gsg.wx.utils.DateUtil;
import com.gsg.wx.utils.HttpUtil;
import com.gsg.wx.utils.LunarCalendarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.*;

/**
 * TODO vx
 *
 * @author shuaigang
 * @date 2022/8/26 12:39
 */
@Slf4j
@Service
public class IWxServiceImpl extends ServiceImpl<WxMapper, User> implements IWxService {

    @Value("${wx.config.appId}")
    private String appId;
    @Value("${wx.config.appSecret}")
    private String appSecret;
    @Value("${calendar.config.calendarAppId}")
    private String calendarAppId;
    @Value("${calendar.config.calendarAppSecret}")
    private String calendarAppSecret;
    @Value("${weather.config.appid}")
    private String weatherAppId;
    @Value("${weather.config.appSecret}")
    private String weatherAppSecret;
    @Value("${message.config.createTime}")
    private String createTime;

    @Autowired
    WxMapper wxMapper;

    private String token = "";

//    public static void main(String[] args) {
//        String s = HttpUtil.sendGet("https://api.tianapi.com/wanan/index", "key=a3b532de9eb4aef865fb63b617f7f286");
//        System.out.println(s);
//        JSONArray newslist = (JSONArray) JSON.parseObject(s).get("newslist");
//        List<String> list = JSONArray.parseArray(newslist.toJSONString(), String.class);
//        String text = JSON.parseObject(list.get(0)).getString("content");
//        System.out.println(text);
//
//    }

    @Override
    public String sendWxMsg() {

        //????????????????????????????????????????????????????????????api
        String grantType = "client_credential";
        //??????????????????
        String params = "grant_type=" + grantType + "&secret=" + appSecret + "&appid=" + appId;
        //??????GET??????
        String send = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(send);
        log.info("??????token????????????=" + jsonObject1);
        //?????? access_token
        token = (String) jsonObject1.get("access_token");

        // ???????????????
        JSONObject calendarData = getCalendarData(1);
        if (calendarData == null) {
            return null;
        }

        List<TempVO> tempList = wxMapper.getTemp("temp0001");
        List<JSONObject> errorList = new ArrayList<>();

        String chp = "?????????????????????????????????????????????????????????";
        String du = "?????????????????????????????????";

        String t1 = getMsg("https://api.shadiao.pro/chp", null, "chp");
        if (t1 != null) {
            chp = t1;
        }
        String t2 = getMsg("https://api.shadiao.pro/du", null, "du");
        if (t2 != null) {
            du = t2;
        }

        for (TempVO temp : tempList) {
            JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());

            templateMsg.put("touser", temp.getWxNumber());
            templateMsg.put("template_id", temp.getTemplate());

            // ???????????????????????????
            JSONObject first = new JSONObject();
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
            String week = DateUtil.getWeekOfDate(new Date());
            String day = date + " " + week;
            first.put("value", day);
            first.put("color", "#EED016");

            // ???????????????????????????????????????????????????
            // city_id=101270101?????????101040100??????
            String temperatureUrl = "https://www.yiketianqi.com/free/day?appid=" + weatherAppId + "&appsecret=" + weatherAppSecret + "&cityid=" + temp.getCityNum() + "&unescape=1";
            String sendGet = HttpUtil.sendGet(temperatureUrl, null);
            JSONObject temperature = JSONObject.parseObject(sendGet);
            String address = "????????????";
            log.error(temp.getCityNum());
            //????????????
            String temDay = "????????????";
            //????????????
            String temNight = "????????????";
            String weatherStatus = "";
            String airTips = "";
            if (temperature.getString("city") != null) {
                temDay = temperature.getString("tem_day") + "??";
                temNight = temperature.getString("tem_night") + "??";
                address = temperature.getString("city");
                weatherStatus = temperature.getString("wea");
                airTips = temperature.getString("air_tips");
            }

            JSONObject city = new JSONObject();
            city.put("value", address);
            city.put("color", "#60AEF2");

            String weather = weatherStatus + ", ?????????" + temNight + " ~ " + temDay;


            JSONObject temperatures = new JSONObject();
            temperatures.put("value", weather);
            temperatures.put("color", "#44B549");

            // ???????????????
            // ????????????????????????
            String typeDes = calendarData.getString("typeDes");
            // ??????
            String chineseZodiac = calendarData.getString("chineseZodiac");
            // ??????
            String solarTerms = calendarData.getString("solarTerms");
            // ????????????
            String lunarCalendar = calendarData.getString("lunarCalendar");
            // ???
            String suit = calendarData.getString("suit");
            // ???
            String avoid = calendarData.getString("avoid");
            // ??????
            String constellation = calendarData.getString("constellation");
            // ????????????????????????
            JSONObject typeDesObj = new JSONObject();
            typeDesObj.put("value", typeDes);
            typeDesObj.put("color", "#0000FF");
            // ??????
            JSONObject chineseZodiacObj = new JSONObject();
            chineseZodiacObj.put("value", chineseZodiac);
            chineseZodiacObj.put("color", "#44B549");
            // ??????
            JSONObject solarTermsObj = new JSONObject();
            solarTermsObj.put("value", solarTerms);
            solarTermsObj.put("color", "#44B549");
            // ????????????
            JSONObject lunarCalendarObj = new JSONObject();
            lunarCalendarObj.put("value", lunarCalendar);
            lunarCalendarObj.put("color", "#44B549");
            // ???
            JSONObject suitObj = new JSONObject();
            suitObj.put("value", suit);
            suitObj.put("color", "#44B549");
            // ???
            JSONObject avoidObj = new JSONObject();
            avoidObj.put("value", avoid);
            avoidObj.put("color", "#DC143C");
            // ??????
            JSONObject constellationObj = new JSONObject();
            constellationObj.put("value", constellation);
            constellationObj.put("color", "#44B549");

            // ????????????????????????
            JSONObject birthDate = new JSONObject();
            String birthDay = "";
            String birthMsg = "????????????";
            try {
                String newD;
                Calendar calendar = Calendar.getInstance();
                if (temp.getBirthday().startsWith("??????")) {
                    newD = LunarCalendarUtil.lunarToSolar(calendar.get(Calendar.YEAR) + "-" + temp.getBirthday().substring(2));
                } else {
                    newD = calendar.get(Calendar.YEAR) + "-" + temp.getBirthday();
                }
                birthDay = DateUtil.daysBetween(date, newD);
                if (Integer.parseInt(birthDay) < 0) {
                    Integer newBirthDay = Integer.parseInt(birthDay) + 365;
                    birthMsg = newBirthDay + "???";
                } else if (Integer.parseInt(birthDay) == 0) {
                    birthMsg = "???????????????????? 0??????????";
                } else {
                    birthMsg = birthDay + "???";
                }
            } catch (ParseException e) {
                log.error("birthDay????????????" + e.getMessage());
            }
            birthDate.put("value", birthMsg);
            birthDate.put("color", "#6EEDE2");


            JSONObject togetherDateObj = new JSONObject();
            String togetherDay = "";
            try {
                togetherDay = "???" + DateUtil.daysBetween(temp.getTogetherDate(), date) + "???";
            } catch (ParseException e) {
                log.error("togetherDate????????????" + e.getMessage());
            }
            togetherDateObj.put("value", togetherDay);
            togetherDateObj.put("color", "#FEABB5");

            JSONObject messageObj = new JSONObject();
            if (Integer.parseInt(birthDay) != 0) {
                if ("chp".equals(temp.getMessage())) {
                    messageObj.put("value", chp);
                } else if ("du".equals(temp.getMessage())) {
                    messageObj.put("value", du);
                } else {
                    messageObj.put("value", temp.getMessage());
                }
            } else {
                messageObj.put("value", "????????????????????????????????????????????????????????????????????????????????????????????????Happy birthday to you???");
            }
            messageObj.put("color", "#C79AD0");

            JSONObject data = new JSONObject(new LinkedHashMap<>());
            data.put("first", first);
            data.put("city", city);
            data.put("temperature", temperatures);

            data.put("typeDes", typeDesObj);
            data.put("chineseZodiac", chineseZodiacObj);
            data.put("solarTerms", solarTermsObj);
            data.put("lunarCalendar", lunarCalendarObj);
            data.put("suit", suitObj);
            data.put("avoid", avoidObj);
            data.put("constellation", constellationObj);

            data.put("togetherDate", togetherDateObj);
            data.put("birthDate", birthDate);
            data.put("message", messageObj);
            data.put("air_tips", airTips);

            templateMsg.put("data", data);
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;

            String sendPost = HttpUtil.sendPost(url, templateMsg.toJSONString());
            JSONObject weChatMsgResult = JSONObject.parseObject(sendPost);
            if (!"0".equals(weChatMsgResult.getString("errcode"))) {
                JSONObject error = new JSONObject();
                error.put("openid", temp.getWxNumber());
                error.put("errorMessage", weChatMsgResult.getString("errmsg"));
                errorList.add(error);
            }
            log.info("sendPost=" + sendPost);
        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();
    }

    @Override
    public String sendWxRemind(String messageId, String round) {

        // ???????????????
        JSONObject calendarData = getCalendarData(0);
        if (calendarData == null) {
            return null;
        }

        //??????????????????
        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        //??????GET??????
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        // ??????????????????????????????json?????????
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("??????token????????????=" + jsonObject1);
        //??????accesstoken
        token = (String) jsonObject1.get("access_token");

        List<TempVO> tempList = wxMapper.getTemp("temp0002");
        Message message = wxMapper.getMessage(messageId);
        List<JSONObject> errorList = new ArrayList<>();
        for (TempVO temp : tempList) {
            JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());

            templateMsg.put("touser", temp.getWxNumber());
            templateMsg.put("template_id", temp.getTemplate());


            JSONObject first = new JSONObject();
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd");

            first.put("value", round);
            first.put("color", "#EED016");

            JSONObject onlineDateObj = new JSONObject();
            String onlineDay = "";
            try {
                onlineDay = DateUtil.daysBetween(createTime, date) + "???";
            } catch (ParseException e) {
                log.error("togetherDate????????????" + e.getMessage());
            }
            onlineDateObj.put("value", onlineDay);
            onlineDateObj.put("color", "#60AEF2");

            JSONObject messageObj = new JSONObject();
            messageObj.put("value", message.getContent());
            messageObj.put("color", "#44B549");


            JSONObject data = new JSONObject(new LinkedHashMap<>());
            data.put("first", first);
            data.put("onlineDate", onlineDateObj);
            data.put("remind", messageObj);


            templateMsg.put("data", data);
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;

            String sendPost = HttpUtil.sendPost(url, templateMsg.toJSONString());
            JSONObject weChatMsgResult = JSONObject.parseObject(sendPost);
            if (!"0".equals(weChatMsgResult.getString("errcode"))) {
                JSONObject error = new JSONObject();
                error.put("openid", temp.getWxNumber());
                error.put("errorMessage", weChatMsgResult.getString("errmsg"));
                errorList.add(error);
            }
            log.info("sendPost=" + sendPost);
        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();
    }

    @Override
    public String sendWxSleep(String messageId) {
        // ???????????????
        JSONObject calendarData = getCalendarData(1);
        if (calendarData == null) {
            return null;
        }

        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("??????token????????????=" + jsonObject1);
        token = (String) jsonObject1.get("access_token");

        List<TempVO> tempList = wxMapper.getTemp("temp0003");


        String msg = "";
        if ("?????????".equals(calendarData.getString("typeDes"))) {
            String ss = getMsg("https://api.tianapi.com/wanan/index", "key=a3b532de9eb4aef865fb63b617f7f286", "sleep");
            if (ss != null) {
                msg = ss;
            } else {
                Message message = wxMapper.getMessage(messageId);
                msg = message.getContent();
            }
        }

        List<JSONObject> errorList = new ArrayList<>();
        for (TempVO temp : tempList) {
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
            String birthDay = "";
            try {
                String newD;
                Calendar calendar = Calendar.getInstance();
                if (temp.getBirthday().startsWith("??????")) {
                    newD = LunarCalendarUtil.lunarToSolar(calendar.get(Calendar.YEAR) + "-" + temp.getBirthday().substring(2));
                } else {
                    newD = calendar.get(Calendar.YEAR) + "-" + temp.getBirthday();
                }
                birthDay = DateUtil.daysBetween(date, newD);
            } catch (ParseException e) {
                log.error("birthDay????????????" + e.getMessage());
            }

            if (!"?????????".equals(calendarData.getString("typeDes"))) {
                if (Integer.parseInt(birthDay) != 0) {
                    continue;
                }
            }
            JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());

            templateMsg.put("touser", temp.getWxNumber());
            templateMsg.put("template_id", temp.getTemplate());


            JSONObject time = new JSONObject();

            time.put("value", date);
            time.put("color", "#EED016");

            JSONObject sleepObj = new JSONObject();
            JSONObject messageObj = new JSONObject();

            if (Integer.parseInt(birthDay) == 0) {
                sleepObj.put("value", "??? ??? ???_??? ????????????????????????????????????????????????");
                messageObj.put("value", "?????????????????????????????????????????????????????????????????????????????????????????????????????????");
            } else {
                sleepObj.put("value", "????????????,???????????????");
                messageObj.put("value", msg);

            }
            sleepObj.put("color", "#60AEF2");
            messageObj.put("color", "#44B549");


            JSONObject data = new JSONObject(new LinkedHashMap<>());
            data.put("time", time);
            data.put("sleep", sleepObj);
            data.put("remind", messageObj);

            templateMsg.put("data", data);
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;

            String sendPost = HttpUtil.sendPost(url, templateMsg.toJSONString());
            JSONObject weChatMsgResult = JSONObject.parseObject(sendPost);
            if (!"0".equals(weChatMsgResult.getString("errcode"))) {
                JSONObject error = new JSONObject();
                error.put("openid", temp.getTemplate());
                error.put("errorMessage", weChatMsgResult.getString("errmsg"));
                errorList.add(error);
            }
            log.info("sendPost=" + sendPost);
        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();
    }

    @Override
    public String sendWxCustomMsg(String id,String message) {
        //????????????????????????????????????????????????????????????api
//        String grantType = "client_credential";
        //??????????????????
        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("??????token????????????=" + jsonObject1);
        token = (String) jsonObject1.get("access_token");

//        List<TempVO> tempList = wxMapper.getTemp("temp0004");
        String tId = wxMapper.getTemplateId("temp0004");
        String vId = wxMapper.getWxNumber(id);
        List<JSONObject> errorList = new ArrayList<>();
//        for (TempVO temp : tempList) {
            JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());

//            templateMsg.put("touser", temp.getWxNumber());
//            templateMsg.put("template_id", temp.getTemplate());
            templateMsg.put("touser", vId);
            templateMsg.put("template_id", tId);

            JSONObject messageObj = new JSONObject();
            messageObj.put("value", message);
            messageObj.put("color", "#44B549");

            JSONObject data = new JSONObject(new LinkedHashMap<>());
            data.put("message", messageObj);

            templateMsg.put("data", data);
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;

            String sendPost = HttpUtil.sendPost(url, templateMsg.toJSONString());
            JSONObject weChatMsgResult = JSONObject.parseObject(sendPost);
            if (!"0".equals(weChatMsgResult.getString("errcode"))) {
                JSONObject error = new JSONObject();
//                error.put("openid", temp.getTemplate());
                error.put("openid", tId);
                error.put("errorMessage", weChatMsgResult.getString("errmsg"));
                errorList.add(error);
            }
            log.info("sendPost=" + sendPost);
//        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();
    }

    /**
     * ??????????????????????????????????????????
     * ???????????????????????????0
     */
    JSONObject getCalendarData(Integer workState) {
        // ???????????????
        String dateParam = DateUtil.formatDate(new Date(), "yyyyMMdd");
        String calendarUrl = "https://www.mxnzp.com/api/holiday/single/" + dateParam;
        String calendarParam = "ignoreHoliday=false&app_id=" + calendarAppId + "&app_secret=" + calendarAppSecret;
        String getCalendar = HttpUtil.sendGet(calendarUrl, calendarParam);
        JSONObject parentCalendar = JSONObject.parseObject(getCalendar);
        System.out.println(parentCalendar);
        String childCalendar = parentCalendar.getString("data");
        JSONObject calendarData = JSONObject.parseObject(childCalendar);

        String work = "?????????";
        String value = "typeDes";
        // ???????????????????????????????????????????????????
        if (workState == 0 && !work.equals(calendarData.getString(value))) {
            return null;
        }
        return calendarData;
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    String getMsg(String url,String param, String name) {
        try {
            String body = HttpUtil.sendGet(url, param);
            if (!"sleep".equals(name)) {
                String text = URLDecoder.decode(JSON.parseObject(JSON.parseObject(body).getString("data")).getString("text"), "UTF-8");
                if ("chp".equals(name) && wxMapper.findChpByText(text) == null) {
                    wxMapper.insertChpText(text);
                    return text;
                }
                if ("du".equals(name) && wxMapper.findDuByText(text) == null) {
                    wxMapper.insertDuText(text);
                    return text;
                }
            } else {
                JSONArray newslist = (JSONArray) JSON.parseObject(body).get("newslist");
                List<String> list = JSONArray.parseArray(newslist.toJSONString(), String.class);
                String text = JSON.parseObject(list.get(0)).getString("content");
                if (wxMapper.findSleepByText(text) == null) {
                    wxMapper.insertSleepText(text);
                    return text;
                }
            }
            return getMsg(url, param, name);
        } catch (Exception e) {
            return null;
        }
    }

}
