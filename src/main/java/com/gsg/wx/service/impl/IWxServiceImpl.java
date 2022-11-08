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

        //这里直接写死就可以，不用改，用法可以去看api
        String grantType = "client_credential";
        //封装请求数据
        String params = "grant_type=" + grantType + "&secret=" + appSecret + "&appid=" + appId;
        //发送GET请求
        String send = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(send);
        log.info("微信token响应结果=" + jsonObject1);
        //拿到 access_token
        token = (String) jsonObject1.get("access_token");

        // 获取万年历
        JSONObject calendarData = getCalendarData(1);
        if (calendarData == null) {
            return null;
        }

        List<TempVO> tempList = wxMapper.getTemp("temp0001");
        List<JSONObject> errorList = new ArrayList<>();

        String chp = "一年之计在于春，一日之计在于晨。早安！";
        String du = "嘿！你他娘的早上好啊！";

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

            // 获取当日日期与星期
            JSONObject first = new JSONObject();
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
            String week = DateUtil.getWeekOfDate(new Date());
            String day = date + " " + week;
            first.put("value", day);
            first.put("color", "#EED016");

            // 获取他所在的城市以及对应的天气温度
            // city_id=101270101成都；101040100重庆
            String temperatureUrl = "https://www.yiketianqi.com/free/day?appid=" + weatherAppId + "&appsecret=" + weatherAppSecret + "&cityid=" + temp.getCityNum() + "&unescape=1";
            String sendGet = HttpUtil.sendGet(temperatureUrl, null);
            JSONObject temperature = JSONObject.parseObject(sendGet);
            String address = "无法识别";
            log.error(temp.getCityNum());
            //最高温度
            String temDay = "无法识别";
            //最低温度
            String temNight = "无法识别";
            String weatherStatus = "";
            String airTips = "";
            if (temperature.getString("city") != null) {
                temDay = temperature.getString("tem_day") + "°";
                temNight = temperature.getString("tem_night") + "°";
                address = temperature.getString("city");
                weatherStatus = temperature.getString("wea");
                airTips = temperature.getString("air_tips");
            }

            JSONObject city = new JSONObject();
            city.put("value", address);
            city.put("color", "#60AEF2");

            String weather = weatherStatus + ", 温度：" + temNight + " ~ " + temDay;


            JSONObject temperatures = new JSONObject();
            temperatures.put("value", weather);
            temperatures.put("color", "#44B549");

            // 获取万年历
            // 节假日或者工作日
            String typeDes = calendarData.getString("typeDes");
            // 属相
            String chineseZodiac = calendarData.getString("chineseZodiac");
            // 节气
            String solarTerms = calendarData.getString("solarTerms");
            // 农历日期
            String lunarCalendar = calendarData.getString("lunarCalendar");
            // 宜
            String suit = calendarData.getString("suit");
            // 忌
            String avoid = calendarData.getString("avoid");
            // 星座
            String constellation = calendarData.getString("constellation");
            // 节假日或者工作日
            JSONObject typeDesObj = new JSONObject();
            typeDesObj.put("value", typeDes);
            typeDesObj.put("color", "#0000FF");
            // 属相
            JSONObject chineseZodiacObj = new JSONObject();
            chineseZodiacObj.put("value", chineseZodiac);
            chineseZodiacObj.put("color", "#44B549");
            // 节气
            JSONObject solarTermsObj = new JSONObject();
            solarTermsObj.put("value", solarTerms);
            solarTermsObj.put("color", "#44B549");
            // 农历日期
            JSONObject lunarCalendarObj = new JSONObject();
            lunarCalendarObj.put("value", lunarCalendar);
            lunarCalendarObj.put("color", "#44B549");
            // 宜
            JSONObject suitObj = new JSONObject();
            suitObj.put("value", suit);
            suitObj.put("color", "#44B549");
            // 忌
            JSONObject avoidObj = new JSONObject();
            avoidObj.put("value", avoid);
            avoidObj.put("color", "#DC143C");
            // 星座
            JSONObject constellationObj = new JSONObject();
            constellationObj.put("value", constellation);
            constellationObj.put("color", "#44B549");

            // 获取距离生日天数
            JSONObject birthDate = new JSONObject();
            String birthDay = "";
            String birthMsg = "无法识别";
            try {
                Calendar calendar = Calendar.getInstance();
                String newD = calendar.get(Calendar.YEAR) + "-" + temp.getBirthday();
                birthDay = DateUtil.daysBetween(date, newD);
                if (Integer.parseInt(birthDay) < 0) {
                    Integer newBirthDay = Integer.parseInt(birthDay) + 365;
                    birthMsg = newBirthDay + "天";
                } else if (Integer.parseInt(birthDay) == 0) {
                    birthMsg = "ฅʕ•̫͡•ʔฅ 0天！🎂";
                } else {
                    birthMsg = birthDay + "天";
                }
            } catch (ParseException e) {
                log.error("birthDay获取失败" + e.getMessage());
            }
            birthDate.put("value", birthMsg);
            birthDate.put("color", "#6EEDE2");


            JSONObject togetherDateObj = new JSONObject();
            String togetherDay = "";
            try {
                togetherDay = "第" + DateUtil.daysBetween(temp.getTogetherDate(), date) + "天";
            } catch (ParseException e) {
                log.error("togetherDate获取失败" + e.getMessage());
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
                messageObj.put("value", "年年岁岁花相似，岁岁年年人不同。醒来惊绝不是梦，眉间皱纹又一重。Happy birthday to you！");
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

        // 获取万年历
        JSONObject calendarData = getCalendarData(0);
        if (calendarData == null) {
            return null;
        }

        //封装请求数据
        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        //发送GET请求
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        // 解析相应内容（转换成json对象）
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("微信token响应结果=" + jsonObject1);
        //拿到accesstoken
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
                onlineDay = DateUtil.daysBetween(createTime, date) + "天";
            } catch (ParseException e) {
                log.error("togetherDate获取失败" + e.getMessage());
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
        // 获取万年历
        JSONObject calendarData = getCalendarData(1);
        if (calendarData == null) {
            return null;
        }

        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("微信token响应结果=" + jsonObject1);
        token = (String) jsonObject1.get("access_token");

        List<TempVO> tempList = wxMapper.getTemp("temp0003");


        String msg = "";
        if ("工作日".equals(calendarData.getString("typeDes"))) {
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
                Calendar calendar = Calendar.getInstance();
                String newD = calendar.get(Calendar.YEAR) + "-" + temp.getBirthday();
                birthDay = DateUtil.daysBetween(date, newD);
            } catch (ParseException e) {
                log.error("birthDay获取失败" + e.getMessage());
            }

            if (!"工作日".equals(calendarData.getString("typeDes"))) {
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
                sleepObj.put("value", "༼ つ ◕_◕ ༽つ：生日也不要忘了按时睡觉哦！");
                messageObj.put("value", "抓住生日的尾巴，深话浅说，长路慢走，未来美好的岁月里愿一切安好，晚安！");
            } else {
                sleepObj.put("value", "充足睡眠,身心健康。");
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
    public String sendWxCustomMsg(String message) {
        //这里直接写死就可以，不用改，用法可以去看api
//        String grantType = "client_credential";
        //封装请求数据
        String params = "grant_type=" + "client_credential" + "&secret=" + appSecret + "&appid=" + appId;
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(sendGet);
        log.info("微信token响应结果=" + jsonObject1);
        token = (String) jsonObject1.get("access_token");

        List<TempVO> tempList = wxMapper.getTemp("temp0004");

        List<JSONObject> errorList = new ArrayList<>();
        for (TempVO temp : tempList) {
            JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());

            templateMsg.put("touser", temp.getWxNumber());
            templateMsg.put("template_id", temp.getTemplate());

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

    /**
     * 获取万年历并判断是否在工作日
     * 不在工作日推送传入0
     */
    JSONObject getCalendarData(Integer workState) {
        // 获取万年历
        String dateParam = DateUtil.formatDate(new Date(), "yyyyMMdd");
        String calendarUrl = "https://www.mxnzp.com/api/holiday/single/" + dateParam;
        String calendarParam = "ignoreHoliday=false&app_id=" + calendarAppId + "&app_secret=" + calendarAppSecret;
        String getCalendar = HttpUtil.sendGet(calendarUrl, calendarParam);
        JSONObject parentCalendar = JSONObject.parseObject(getCalendar);
        System.out.println(parentCalendar);
        String childCalendar = parentCalendar.getString("data");
        JSONObject calendarData = JSONObject.parseObject(childCalendar);

        String work = "工作日";
        String value = "typeDes";
        // 如果不是工作日，则不进行微信推送。
        if (workState == 0 && !work.equals(calendarData.getString(value))) {
            return null;
        }
        return calendarData;
    }

    /**
     * 获取每日彩虹屁、心灵鸡汤、睡觉提示语
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
