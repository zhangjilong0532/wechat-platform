package com.platform.api;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.cache.J2CacheUtils;
import com.platform.entity.*;
import com.platform.service.*;
import com.platform.util.ApiBaseAction;
import com.platform.util.wechat.WechatUtil;
import com.platform.utils.Base64;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/wx/msg")
public class ApiMessageController {

    @Autowired
    private ApiMessageService wxService;

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/sendTempMess", method = {RequestMethod.GET, RequestMethod.POST})
    public void sendTempMess(HttpServletRequest request, HttpServletResponse response) {
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        if (isGet) {//首次验证token
            // 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // 随机字符串
            String echostr = request.getParameter("echostr");
            PrintWriter out = null;
            try {
                out = response.getWriter();
                // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，否则接入失败  
                if (WechatUtil.checkSignature(signature, timestamp, nonce)) {
                    logger.info("成功");
                    out.print(echostr);
                    out.flush(); //必须刷新
                }
                logger.info("失败");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                out.close();
            }
        } else {//进行客服操作
            try {
//            JSON.toJSONString(Tools.resolveParam(request));
                //log.info("小程序返回消息：{}" , JSON.toJSONString(Tools.resolveParam(request)));
                // 进入POST聊天处理
                // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
                request.setCharacterEncoding("UTF-8");
                response.setCharacterEncoding("UTF-8");
                // 接收消息并返回消息
                String result = wxService.acceptMessage(request, response);
                // 响应消息
                PrintWriter out = response.getWriter();
                response.reset();
                out.close();
            } catch (Exception ex) {
                logger.error("微信帐号接口配置失败！", ex);
                ex.printStackTrace();
            }
        }
    }

}
