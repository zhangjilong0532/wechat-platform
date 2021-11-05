package com.platform.api;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.UserVo;
import com.platform.oss.OSSFactory;
import com.platform.service.ApiUserService;
import com.platform.service.TokenService;
import com.platform.util.ApiBaseAction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 *  分销二维码
 *
 * @author lipengjun
 * @email
 * * @date 2017-03-23 15:47
 */
@Api(tags = "分销二维码")
@RestController
@RequestMapping("/api/qrCode")
public class ApiQrCodeController extends ApiBaseAction{

    @Autowired
    private ApiUserService userService;

    @Autowired
    private TokenService tokenService;


    /**
     * 生成二维码
     */
    @ApiOperation(value = "生成二维码")
    @RequestMapping("smallProgramCode")
    public Object smallProgramCode(@LoginUser UserVo loginUser) {
        String url = "";
        try {
            url = tokenService.createQrCode(loginUser.getUserId()+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toResponsSuccess(url);
    }


}
