package org.roy.core.request;

import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.Request;

/**
 * @author: roy
 * @date: 2023/10/22 12:50
 * @description:
 */
public interface IGatewayRequest {

    //修改目标服务地址
    void  setModifyHost(String host);

    //获取目标地址
    String getModifyHost();

    //设置目标路径
    void  setModifyPath(String path);

    //获取目标路径
    String getModifyPath();

    //添加请求头信息
    void  addHeader(CharSequence name,String value);

    //设置请求头信息
    void setHeader(CharSequence name,String value);


    //添加请求参数 get请求
    void  addQueryParam(String name,String value);


    //添加请求参数 post
    void  addFormParam(String name,String value);

    //添加与替换cookie
    void addOrReplaceCookie(Cookie cookie);

    //设置超时时间
    void  setRequestTimeOut(int requestTimeOut);

    //获取最终请求路径包含请求参数,Http://localhost:8081/api/admin?name=111......
    String  getFinalUrl();

    Request build();
}



