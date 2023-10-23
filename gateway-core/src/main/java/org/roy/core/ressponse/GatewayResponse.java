package org.roy.core.ressponse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;
import org.roy.common.enums.ResponseCode;
import org.roy.common.util.JSONUtil;

import java.util.jar.JarEntry;

/**
 * @author: roy
 * @date: 2023/10/22 12:48
 * @description:
 */
@Data
public class GatewayResponse {

    /*
    * 响应头
    * */
    private HttpHeaders responseHeader=new DefaultHttpHeaders();


    /*
    * 额外的响应头
    * */
    private HttpHeaders extraResponseHeader=new DefaultHttpHeaders();

    /*
    * 响应内容
    * */
    private  String context;

    /*
    * 响应码
    * */
    private HttpResponseStatus httpResponseStatus;

    private Response futureResponse;

    public GatewayResponse() {
    }

    /**
     * 设置请求头
     * @param key
     * @param val
     */
    public  void  putHeader(CharSequence key,CharSequence val){
        responseHeader.add(key,val)
                ;
    }

    /**
     * 构建异步返回对象
     * @param futureResponse
     * @return
     */
    public  static  GatewayResponse BuildGatewayResponse(Response futureResponse){
        GatewayResponse response=new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return  response;
    }


    /**
     *  返回一个调用失败的JOSN对象
     * @param code
     * @param arg
     * @return
     */
    public  static  GatewayResponse BuildGatewayResponse(ResponseCode code, Object...arg){

        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS,code.getStatus().code());
        objectNode.put(JSONUtil.CODE,code.getCode());
        objectNode.put(JSONUtil.MESSAGE,code.getMessage());
        GatewayResponse gatewayResponse=new GatewayResponse();
        gatewayResponse.setHttpResponseStatus(code.getStatus());
        gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        gatewayResponse.setContext(JSONUtil.toJSONString(objectNode));
        return gatewayResponse;
    }

    /**
     * 返回成功对象
     * @param data
     * @return
     */
    public  static  GatewayResponse BuildGatewayResponse(Object data){

        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS,ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE,ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA,data);
        GatewayResponse gatewayResponse=new GatewayResponse();
        gatewayResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        gatewayResponse.setContext(JSONUtil.toJSONString(objectNode));
        return gatewayResponse;
    }
}
