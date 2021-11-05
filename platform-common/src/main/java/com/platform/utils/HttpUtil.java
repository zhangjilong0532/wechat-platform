package com.platform.utils;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作者: @author Harmon <br>
 * 时间: 2016-09-01 09:18<br>
 * 描述: Http请求通用工具 <br>
 */
public class HttpUtil {

    // 日志
    private static final Logger logger = Logger.getLogger(HttpUtil.class);
    /**
     * 定义编码格式 UTF-8
     */
    public static final String URL_PARAM_DECODECHARSET_UTF8 = "UTF-8";

    /**
     * 定义编码格式 GBK
     */
    public static final String URL_PARAM_DECODECHARSET_GBK = "GBK";

    private static final String URL_PARAM_CONNECT_FLAG = "&";

    private static final String EMPTY = "";

    private static MultiThreadedHttpConnectionManager connectionManager = null;

    private static int connectionTimeOut = 25000;

    private static int socketTimeOut = 25000;

    private static int maxConnectionPerHost = 20;

    private static int maxTotalConnections = 20;

    private static HttpClient client;
    // 当前登录的人
    static Cookie[] cookies = null;
    // 登录URL
    static String loginURL = "/admin/login.do";
    // 登录账户
    static String loginUserName = "admin";
    static String loginPassword = "admin";

    static {
        connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setConnectionTimeout(connectionTimeOut);
        connectionManager.getParams().setSoTimeout(socketTimeOut);
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
        connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
        client = new HttpClient(connectionManager);
    }

    /**
     * POST方式提交数据
     *
     * @param url    待请求的URL
     * @param params 要提交的数据
     * @param enc    编码
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String URLPost(String url, Map<String, Object> params, String enc) {

        String response = EMPTY;
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(url);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
            //将表单的值放入postMethod中
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                Object value = params.get(key);
                postMethod.addParameter(key, String.valueOf(value));
            }
            //执行postMethod
            int statusCode = client.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                response = postMethod.getResponseBodyAsString();
            } else {
                logger.error("响应状态码 = " + postMethod.getStatusCode());
            }
        } catch (HttpException e) {
            logger.error("发生致命的异常，可能是协议不对或者返回的内容有问题", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("发生网络异常", e);
            e.printStackTrace();
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                postMethod = null;
            }
        }

        return response;
    }

    /**
     * GET方式提交数据
     *
     * @param url    待请求的URL
     * @param params 要提交的数据
     * @param enc    编码
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String URLGet(String url, Map<String, String> params, String enc) {

        String response = EMPTY;
        GetMethod getMethod = null;
        StringBuffer strtTotalURL = new StringBuffer(EMPTY);

        if (strtTotalURL.indexOf("?") == -1) {
            strtTotalURL.append(url).append("?").append(getUrl(params, enc));
        } else {
            strtTotalURL.append(url).append("&").append(getUrl(params, enc));
        }
        logger.debug("GET请求URL = \n" + strtTotalURL.toString());

        try {
            getMethod = new GetMethod(strtTotalURL.toString());
            getMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
            //执行getMethod
            int statusCode = client.executeMethod(getMethod);
            if (statusCode == HttpStatus.SC_OK) {
                response = getMethod.getResponseBodyAsString();
            } else {
                logger.debug("响应状态码 = " + getMethod.getStatusCode());
            }
        } catch (HttpException e) {
            logger.error("发生致命的异常，可能是协议不对或者返回的内容有问题", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("发生网络异常", e);
            e.printStackTrace();
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
                getMethod = null;
            }
        }

        return response;
    }

    /**
     * 据Map生成URL字符串
     *
     * @param map      Map
     * @param valueEnc URL编码
     * @return URL
     */
    private static String getUrl(Map<String, String> map, String valueEnc) {

        if (null == map || map.keySet().size() == 0) {
            return (EMPTY);
        }
        StringBuffer url = new StringBuffer();
        Set<String> keys = map.keySet();
        for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
            String key = it.next();
            if (map.containsKey(key)) {
                String val = map.get(key);
                String str = val != null ? val : EMPTY;
                try {
                    str = URLEncoder.encode(str, valueEnc);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                url.append(key).append("=").append(str).append(URL_PARAM_CONNECT_FLAG);
            }
        }
        String strURL = EMPTY;
        strURL = url.toString();
        if (URL_PARAM_CONNECT_FLAG.equals(EMPTY + strURL.charAt(strURL.length() - 1))) {
            strURL = strURL.substring(0, strURL.length() - 1);
        }

        return (strURL);
    }

    /**
     * POST方式提交数据
     *
     * @param url     待请求的URL
     * @param params  要提交的数据
     * @param enc     编码
     * @param session 带session
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String URLPost(String url, Map<String, Object> params, String enc, boolean session) {

        String response = EMPTY;
        PostMethod postMethod = null;
        if (!session) {
            return URLPost(url, params, enc);
        }
        try {
            postMethod = new PostMethod(url);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
            //将表单的值放入postMethod中
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                Object value = params.get(key);
                postMethod.addParameter(key, String.valueOf(value));
            }
            if (null != cookies) {
                client.getState().addCookies(cookies);
            } else {
                getAuthCookie(url, enc);
                client.getState().addCookies(cookies);
            }
            //执行postMethod
            int statusCode = client.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                response = postMethod.getResponseBodyAsString();
            } else {
                logger.error("响应状态码 = " + postMethod.getStatusCode());
            }
        } catch (HttpException e) {
            logger.error("发生致命的异常，可能是协议不对或者返回的内容有问题", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("发生网络异常", e);
            e.printStackTrace();
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                postMethod = null;
            }
        }

        return response;
    }

    /**
     * 获取session
     *
     * @param url 待请求的URL
     * @param enc 编码
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static void getAuthCookie(String url, String enc) {

        PostMethod postMethod = null;
        try {
            Object[] ipPort = getIpPortFormURL(url);
            String ip = (String) ipPort[0];
            int port = (Integer) ipPort[1];
            String logUrl = "http://" + ip + ":" + port + loginURL;
            postMethod = new PostMethod(logUrl);
            postMethod.addParameter("username", loginUserName);
            postMethod.addParameter("password", loginPassword);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
            //执行postMethod
            int statusCode = client.executeMethod(postMethod);
            // 查看 cookie 信息
            CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
            cookies = cookiespec.match(ip, port, "/", false, client.getState().getCookies());
            logger.error("响应状态码 = " + postMethod.getStatusCode());
        } catch (HttpException e) {
            logger.error("发生致命的异常，可能是协议不对或者返回的内容有问题", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("发生网络异常", e);
            e.printStackTrace();
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                postMethod = null;
            }
        }
    }

    /**
     * 解析URL的端口和ip
     *
     * @param URL
     * @return
     */
    public static Object[] getIpPortFormURL(String URL) {
        Object[] ip_port = new Object[2];
        try {
            java.net.URL url = new java.net.URL(URL);
            ip_port[0] = url.getHost();
            ip_port[1] = url.getPort() != -1 ? url.getPort() : 80;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ip_port;
    }

    public static void setLoginPassword(String loginPassword) {
        HttpUtil.loginPassword = loginPassword;
    }

    public static void setLoginUserName(String loginUserName) {
        HttpUtil.loginUserName = loginUserName;
    }

    public static void setLoginURL(String loginURL) {
        HttpUtil.loginURL = loginURL;
    }

    public static void main(String[] args) throws MalformedURLException {
        java.net.URL url = new java.net.URL("http://blog.csdn.net/zhujianlin1990");
        System.out.println(url.getHost());
        System.out.println(url.getPort());
    }

    /**
     * http请求工具类，get请求
     *
     * @param url
     * @param params
     * @param resonseCharSet
     * @return
     * @throws Exception
     */
    public static String httpGet(String url, Map<String, Object> params, String... resonseCharSet) throws IOException {
        DefaultHttpClient defaultHttpClient = null;
        BufferedReader bufferedReader = null;
        try {
            defaultHttpClient = new DefaultHttpClient();
            if (params != null) {
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<String> iterator = params.keySet().iterator();
                String key;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    Object val = params.get(key);
                    if (val instanceof List) {
                        List v = (List) val;
                        for (Object o : v) {
                            stringBuilder.append(key).append("=").append(o.toString()).append("&");
                        }
                    } else {
                        stringBuilder.append(key).append("=").append(val.toString()).append("&");
                    }
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                url = url + "?" + stringBuilder.toString();
                logger.info("url:{}"+ url);
            }
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json;charset=ut-8");
            HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                String errorLog = "请求失败，errorCode:" + httpResponse.getStatusLine().getStatusCode();
                logger.info(errorLog);
//                throw new HttpRequestException(url + errorLog);
                return "fail";
            }
            //读取返回信息
            String charSet = "utf-8";
            if (resonseCharSet != null && resonseCharSet.length > 0)
                charSet = resonseCharSet[0];
            String output;
            bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), charSet));

            StringBuilder dataBuilder = new StringBuilder();
            while ((output = bufferedReader.readLine()) != null) {
                dataBuilder.append(output);
            }
            return dataBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (defaultHttpClient != null)
                defaultHttpClient.getConnectionManager().shutdown();
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }


    /**
     * http请求工具类，post请求
     *
     * @param url    url
     * @param param 参数值 仅支持String
     * @return
     * @throws Exception
     */
    public static String httpPost(String url, String param) throws IOException {
        DefaultHttpClient defaultHttpClient = null;
        BufferedReader bufferedReader = null;
        try {
            defaultHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=ut-8");
            if (StringUtils.isNotEmpty(param)) {
                logger.info("参数值：{}"+ param);
                HttpEntity httpEntity = new StringEntity(param, "utf-8");
                httpPost.setEntity(httpEntity);
            }
            HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                String errorLog = "请求失败，errorCode:" + httpResponse.getStatusLine().getStatusCode();
                logger.info(errorLog);
//                throw new HttpRequestException(url + errorLog);
                return "fail";
            }
            //读取返回信息
            String output;
            bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
            StringBuilder stringBuilder = new StringBuilder();
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (defaultHttpClient != null)
                defaultHttpClient.getConnectionManager().shutdown();
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }





}
