package org.roy.core.request;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.request.body.Body;
import org.roy.common.constants.BasicConst;
import org.roy.common.util.TimeUtil;

import javax.annotation.processing.Generated;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author: roy
 * @date: 2023/10/22 12:48
 * @description:
 */
@Data
public class GatewayRequest implements  IGatewayRequest{
    //服务唯一id
    private  final  String uniqueId;
    //进入网关开始时间
    @Getter
    private  final  long beginTime;
    //进入网关的结束时间
    @Getter
    private  final  long endTime;

    //字符集
    @Getter
    private final Charset charset;

    //客户端的IP地址
    @Getter
    private  final  String clientIP;

    //服务端主机名
    @Getter
    private  final String host;

    //服务端的请求路径 /XXX/XX/X
    @Getter
    private  final  String path;


    //同一资源标识符 /XXX/XX/X?attr=1
    @Getter
    private  final  String URI;

    //请求方式 Post/Get/Put
    @Getter
    private  final HttpMethod httpMethod;

    //请求格式
    @Getter
    private  final  HttpMethod contentType;

    //请求头
    @Getter
    private  final HttpHeaders httpHeader;

    //参数解析器
    @Getter
    private final QueryStringDecoder queryStringDecoder;

    //合法请求
    @Getter
    private  final  FullHttpRequest fullHttpRequest;

    //请求体
    private   Map<String,Cookie> cookieMap;

    private String body;

    //post请求参数
    private  Map<String, List<String>> postParameters;

    //可修改的Scheme 默认为: Http://
    private String modifyScheme;

    //可修改的主机地址
    private  String modifyHost;

    //可修改的主机路径
    private  String modifyPath;

    //构建下游请求时的Http构建器
    private  final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId, long beginTime,
                          long endTime, Charset charset, String clientIP,
                          String host, String path, String URI,
                          HttpMethod httpMethod, HttpMethod contentType,
                          HttpHeaders httpHeader, QueryStringDecoder queryStringDecoder,
                          FullHttpRequest fullHttpRequest, RequestBuilder requestBuilder) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.endTime = endTime;
        this.charset = charset;
        this.clientIP = clientIP;
        this.host = host;
        this.URI = URI;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.httpHeader = httpHeader;
        this.queryStringDecoder = new QueryStringDecoder(URI,charset);
        this.fullHttpRequest = fullHttpRequest;
        this.path=queryStringDecoder.path();
        this.requestBuilder =new RequestBuilder();

        this.modifyHost=host;
        this.modifyPath=path;
        this.modifyScheme= BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder.setMethod(getHttpMethod().name());
        this.requestBuilder.setHeaders(getHttpHeader());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());

        ByteBuf contentBuffer=fullHttpRequest.content();
        if(Objects.nonNull(contentBuffer)){
            this.requestBuilder.setBody(contentBuffer.nioBuffer());
        }


    }

    //获取body
    public  String getBody(){
        if (StringUtils.isEmpty(body)){
            body=fullHttpRequest.content().toString(charset);
        }
        return  body;
    }

    //获取Cookie
    public Cookie getCookie(String name){
        if(cookieMap==null){
            cookieMap=new HashMap<>();
            String cookieStr=getHttpHeader().get(HttpHeaderNames.COOKIE);
            Set<Cookie> cookies= ServerCookieDecoder.STRICT.decode(cookieStr);
            for(Cookie cookie:cookies){
                cookieMap.put(name,cookie);
            }
        }
        return  cookieMap.get(name)
                ;
    }

    //获取请求参数值
    public  List<String> getQueryParametersMultiple(String name){
        return queryStringDecoder.parameters().get(name);
    }

    public  List<String> getFormParametersMultiple(String name){
        String Body=getBody();
        if (isFormPost()){
            if(postParameters==null){
                QueryStringDecoder paramDecoder=new QueryStringDecoder(body,false);
                postParameters=paramDecoder.parameters();
            }
            if(postParameters==null||postParameters.isEmpty()) {
                return null;
            }else {
                return  postParameters.get(name);
            }
        }else if(isJsonPost()){
            return  Lists.newArrayList(JsonPath.read(body,name).toString());
        }
        return queryStringDecoder.parameters().get(name);
    }

    private boolean isFormPost() {
        return HttpMethod.POST.equals(httpMethod) &&
                (contentType.asciiName().startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.asciiName().startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    private boolean isJsonPost() {
        return HttpMethod.POST.equals(httpMethod) &&
                contentType.asciiName().startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Override
    public void setModifyHost(String host) {

    }

    @Override
    public String getModifyHost() {
        return null;
    }

    @Override
    public void setModifyPath(String path) {

    }

    @Override
    public String getModifyPath() {
        return null;
    }

    @Override
    public void addHeader(CharSequence name, String value) {

    }

    @Override
    public void setHeader(CharSequence name, String value) {

    }

    @Override
    public void addQueryParam(String name, String value) {

    }

    @Override
    public void addFormParam(String name, String value) {

    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {

    }

    @Override
    public void setRequestTimeOut(int requestTimeOut) {

    }

    @Override
    public void getFinalUrl() {

    }

    @Override
    public Request build() {
        return null;
    }
}
