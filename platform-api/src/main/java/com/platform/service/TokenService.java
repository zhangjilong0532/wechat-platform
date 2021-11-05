package com.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.platform.cache.J2CacheUtils;
import com.platform.dao.ApiTokenMapper;
import com.platform.entity.TokenEntity;
import com.platform.oss.OSSFactory;
import com.platform.util.ApiUserUtils;
import com.platform.util.RedisUtils;
import com.platform.utils.CharUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class TokenService {
    @Autowired
    private ApiTokenMapper tokenDao;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;
    
    //12小时后过期
    private final static int EXPIRE = 3600 * 12;
    //2小时后过期
    private final static int EXPIRE2 = 3600 * 2;

    public TokenEntity queryByUserId(Long userId) {
        return tokenDao.queryByUserId(userId);
    }

    public TokenEntity queryByToken(String token) {
        return tokenDao.queryByToken(token);
    }

    public void save(TokenEntity token) {
        tokenDao.save(token);
    }

    public void update(TokenEntity token) {
        tokenDao.update(token);
    }

    public Map<String, Object> createToken(long userId) {
        //生成一个token
        String token = CharUtil.getRandomString(32);
        //当前时间
        Date now = new Date();

        //过期时间
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

        //判断是否生成过token
        TokenEntity tokenEntity = queryByUserId(userId);
        if (tokenEntity == null) {
            tokenEntity = new TokenEntity();
            tokenEntity.setUserId(userId);
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);

            //保存token
            save(tokenEntity);
        } else {
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);

            //更新token
            update(tokenEntity);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", EXPIRE);

        return map;
    }


    public String getAccessToken() {
    	String access_token = null;
    	
    	boolean flag = RedisUtils.exists(J2CacheUtils.SYSTEM_ACCESS_TOKEN);
    	if(flag == true) {
    		access_token = RedisUtils.get(J2CacheUtils.SYSTEM_ACCESS_TOKEN).toString();
        	if(StringUtils.isNotBlank(access_token)) {
        		return access_token;
        	}
    	}
    	
    	
    	//获取access_token
        String requestUrl = ApiUserUtils.getAccessToken();
        String res = restTemplate.getForObject(requestUrl, String.class);
        JSONObject sessionData = JSON.parseObject(res);
        access_token=sessionData.getString("access_token");
        
        RedisUtils.set(J2CacheUtils.SYSTEM_ACCESS_TOKEN, access_token, EXPIRE2);
        return access_token;
    }


//    private String file = "F:/";
    private String file = "/usr/local/pic/";

    /**
     * 生成分销二维码
     */
    public  String createQrCode(String userId){
        String url = "";
        try{
            this.create(userId);
            File a = new File(file+userId+".png");
            InputStream inputStream = new FileInputStream(a);
            url = OSSFactory.build().upload(inputStream, "qrcode/"+userId+".png");
        }catch (Exception e){

        }
        return url;
    }


    /**
     * 生成分销二维码
     */
    public void create(String fileName) {
        try
        {
            String access_token = this.getAccessToken();

            URL url = new URL("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+access_token);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");// 提交模式
            // conn.setConnectTimeout(10000);//连接超时 单位毫秒
            // conn.setReadTimeout(2000);//读取超时 单位毫秒
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            // 发送请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("scene", fileName);
            paramJson.put("page", "pages/ucenter/index/index");
            paramJson.put("width", 430);
            paramJson.put("auto_color", true);

            printWriter.write(paramJson.toString());
            // flush输出流的缓冲
            printWriter.flush();
            //开始获取数据
            BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
            OutputStream os = new FileOutputStream(new File(file+fileName+".png"));

            int len;
            byte[] arr = new byte[1024];
            while ((len = bis.read(arr)) != -1)
            {
                os.write(arr, 0, len);
                os.flush();
            }
            os.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
