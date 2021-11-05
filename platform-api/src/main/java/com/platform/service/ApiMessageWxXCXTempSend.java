//package com.platform.service;
//
//import com.alibaba.fastjson.JSON;
//import com.platform.util.RedisUtils;
//import com.platform.utils.HttpUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Map;
//
///**
// * Created by Administrator on 2020/3/6.
// */
//@Component
//public class ApiMessageWxXCXTempSend {
//
//    private Logger logger = LoggerFactory.getLogger(ApiMessageWxXCXTempSend.class);
//
//
//    @Autowired
//    private RedisUtils redisLite;
//
//    private static String TEMP_URL = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";
//
//    public static final String APP_ID = "wx3162b692c201b28d";
//    private static final String APP_SECRET = "e0c4003123852db12f770000dac5f6cf";
//
//    /**
//     * 获取小程序token，（ps:0=token获取失败）
//     *
//     * @return
//     */
//    public String getAccessToken() {
//
//        String accessToken = "0";
//        try {
//            //此处APP_ID APP_SECRET  在微信小程序后端可见
//            String accessTokenUrl = String.format(TEMP_URL, APP_ID, APP_SECRET);
//            String result = HttpUtil.httpGet(accessTokenUrl, null, null);
//            Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
//            if (resultMap.containsKey("access_token")) {
//                accessToken = resultMap.get("access_token").toString();
//            }
//        } catch (IOException ioe) {
//            logger.error("小程序http请求异常");
//            ioe.printStackTrace();
//        }
//        return accessToken;
//    }
//
//}
