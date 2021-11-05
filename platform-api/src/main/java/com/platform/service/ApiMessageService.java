package com.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.platform.dao.ApiKeywordsMapper;
import com.platform.entity.WxMsgBean;
import com.platform.util.QRCodeUtils;
import com.platform.utils.HttpUtil;
import com.platform.utils.XmlUtil;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Service
public class ApiMessageService {

    private Logger logger = LoggerFactory.getLogger(ApiMessageService.class);

    private static String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send";

    @Autowired
    private TokenService tokenService;


//    @Autowired
//    public ApiMessageService(UserDao userDao) {
//        super(userDao);
//    }


    public String acceptMessage(HttpServletRequest request, HttpServletResponse response) {
//        {CreateTime=1548042266, Event=user_enter_tempsession, ToUserName=gh_e6198220cbff,
//                FromUserName=oZvme4q2Oi7Dz3FChXc43kqw28, MsgType=event, SessionFrom=wxapp}
        String respMessage = "";
        try {
            // xml请求解析
            Map<String, String> requestMap = XmlUtil.parseXml(request);
            logger.info(">>>>>>>>>>>>>"+requestMap);
//            requestMap.put("FromUserName","oo4i55Sc3rKgW4l4WHdVPyb5X6NY");
//            requestMap.put("CreateTime","1583477168");
//            requestMap.put("ToUserName","gh_097d82778478");
//            requestMap.put("MsgType","transfer_customer_service");

            // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");
            // 消息类型
            String msgType = requestMap.get("MsgType");
            //人工服务
            String content = requestMap.get("Content");
            String accessToken=getAccessToken();

            //此处我默认为直接人工服务  可根据实际业务调整
            return switchCustomerService(fromUserName,toUserName,requestMap);
            //小程序客服 文本信息
           /* if(msgType.equals("text")){
                if("人工服务".equals(content)){
                    HashMap<String, Object> resultMap = new HashMap<>();
                     resultMap.put("ToUserName",fromUserName);
                     resultMap.put("FromUserName",toUserName);
                     resultMap.put("CreateTime", requestMap.get("CreateTime"));
                     resultMap.put("MsgType","transfer_customer_service");
                     String json = JSON.toJSONString(resultMap);
                     JSONObject result = JSONObject.parseObject(json);
                     logger.info("POST   result"  +  result);
                     return result;
                }
                sendCustomerTextMessage(fromUserName,"你好，欢迎使用人工服务",accessToken);
            }else if(msgType.equals("event")){//会话功能
                String sessionFrom = (String) requestMap.get("SessionFrom");
                logger.info("SessionFrom   SessionFrom"  +  sessionFrom);
                int i = sessionFrom.indexOf("+");
                String sessionFromFirst = "1";
                String appId = wxXCXTempSend.APP_ID;
                if( i > 0){
                    sessionFromFirst = sessionFrom.substring(0, i); //标志位 1 2 3 4 5 6
                    logger.info("SessionFrom   sessionFromFirst    "  +  sessionFromFirst);
                    String sessionFromLast = sessionFrom.substring(i+1);  //{"appId":"","data":"test"}
                    logger.info("SessionFrom   sessionFromLast     "  +  sessionFromLast);
                    if(JSONObject.parseObject(sessionFromLast).get("appId") != null){
                        appId = (String) JSONObject.parseObject(sessionFromLast).get("appId");
                    }
                    sendCustomerTextMessage(fromUserName,"你好，欢迎使用会话服务",accessToken);
                }

            }else if(msgType.equals("image")){
                logger.info("公众号接受图片..........");
                sendCustomerImageMessage(fromUserName,requestMap.get("MediaId"),accessToken);
            }else{

            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return respMessage;
    }

    private String getAccessToken() {
        String accessToken = null;
        try {
            accessToken = QRCodeUtils.getToken();
            if ("0".equals(accessToken)) {
                logger.error("微信小程序获取token调用失败");
                accessToken = "0";
            } else if ("1".equals(accessToken)) {
                logger.error("微信小程序获取token系统繁忙");
                accessToken = "0";
            } else if ("2".equals(accessToken)) {
                logger.error("微信小程序获取token，AppSecret错误或者AppID不合法");
                accessToken = "0";
            }
        } catch (Exception e) {
            logger.error("微信小程序获取token异常");
            e.printStackTrace();
            accessToken = "0";
        }
        return accessToken;
    }

    /**
     * 文本事件
     * */
    public  String sendCustomerTextMessage(String openid,String text,String accessToken)throws Exception{
        Map<String,Object> map_content = new HashMap<>();
        map_content.put("content",text);
        Map<String,Object> map = new HashMap<>();
        map.put("touser",openid);
        map.put("msgtype","text");
        map.put("text",map_content);
        String content =  JSON.toJSONString(map);
        return HttpUtil.httpPost(SEND_URL+"?access_token="+accessToken,content);

    }

    /**
     * 会话事件
     * */
    public  String sendFirstMessage(String openid, String text,String accessToken) throws Exception {

        Map<String, Object> map_content = new HashMap<>();
        map_content.put("content", text);

        Map<String, Object> map = new HashMap<>();
        map.put("touser", openid);
        map.put("msgtype", "text");
        map.put("text", map_content);

        String content = JSON.toJSONString(map);
        return HttpUtil.httpPost(SEND_URL + "?access_token=" + accessToken, content);
    }

    /***
     * 文档地址：https://mp.weixin.qq.com/debug/wxadoc/dev/api/custommsg/conversation.html
     * 发送的图片消息
     */
    public  String sendCustomerImageMessage (String openid, String mediaId,String accessToken)throws Exception{
        Map<String, Object> map_content = new HashMap<>();
        map_content.put("media_id", mediaId);
        Map<String, Object> map = new HashMap<>();

        map.put("touser", openid);
        map.put("msgtype", "image");

        map.put("image", map_content);
        String content = JSON.toJSONString(map);
        return HttpUtil.httpPost(SEND_URL + "?access_token=" + accessToken, content);
    }


    /***
     * 文档地址：https://mp.weixin.qq.com/debug/wxadoc/dev/api/custommsg/conversation.html
     * 转发至人工客服 现在默认都为人工客服
     */
    public  String switchCustomerService  (String fromUserName, String toUserName,Map requestMap)throws Exception {

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("ToUserName", fromUserName);
        resultMap.put("FromUserName", toUserName);
        resultMap.put("CreateTime", requestMap.get("CreateTime"));
        resultMap.put("MsgType", "transfer_customer_service");
//        String json = JSON.toJSONString(resultMap);
//        JSONObject result = JSONObject.parseObject(json);

        WxMsgBean wxMsgBean = new WxMsgBean();
        wxMsgBean.setCreateTime(resultMap.get("CreateTime")+"");
        wxMsgBean.setFromUserName(toUserName);
        wxMsgBean.setToUserName(fromUserName);
        wxMsgBean.setMsgType("transfer_customer_service");

        String  result = XmlUtil.converToXml(wxMsgBean,"UTF-8").
                replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>","");
        logger.info("POST   result:" + result);
        return "success";

    }

}
