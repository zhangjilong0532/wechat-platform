package com.platform.entity;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


/**
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2017-08-15 08:03:39
 */
@XmlRootElement(name = "xml",namespace = "")
public class WxMsgBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String CreateTime ;

    private String ToUserName ;

    private String FromUserName ;

    private String MsgType ;


    public String getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
    }

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        ToUserName = toUserName;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }
}
