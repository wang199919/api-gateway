package org.roy.core.helper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.config.DynamicConfigManager;
import org.roy.common.config.HttpServiceInvoker;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInvoker;
import org.roy.common.constants.BasicConst;
import org.roy.common.constants.GatewayConst;
import org.roy.common.constants.GatewayProtocol;
import org.roy.common.exception.ResponseException;
import org.roy.common.rules.Rule;
import org.roy.core.context.GatewayContext;
import org.roy.core.request.GatewayRequest;


import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.roy.common.enums.ResponseCode.PATH_NO_MATCHED;


public class RequestHelper {

	public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
		
		//	构建请求对象GatewayRequest
		GatewayRequest gateWayRequest = doRequest(request, ctx);
		
		//	根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
		ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(gateWayRequest.getUniqueId());

		
		//	根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
		ServiceInvoker serviceInvoker = new HttpServiceInvoker();
		serviceInvoker.setInvokerPath(gateWayRequest.getPath());
		serviceInvoker.setTimeout(500);

		//获取Rule

		Rule rule=getRule(gateWayRequest,serviceDefinition.getServiceId());

		//	构建我们而定GateWayContext对象
		GatewayContext gatewayContext = new GatewayContext(
				serviceDefinition.getProtocol(),
				ctx,
				HttpUtil.isKeepAlive(request),
				gateWayRequest,
				new Rule());


		//后续服务发现做完，这里都要改成动态的
		//gatewayContext.getRequest().setModifyHost("127.0.0.1:8080");
		return gatewayContext;
	}

	private static Rule getRule(GatewayRequest request,String serviceID) {
		String key= serviceID+"."+request.getPath();
		DynamicConfigManager instance = DynamicConfigManager.getInstance();

		Rule rule=instance.getRuleByPath(key);
		if (rule!=null)return  rule;
		return instance.getRuleByServiceId(serviceID).stream().filter(r->request.getPath().startsWith(r.getPrefix())).findAny().orElseThrow(()-> new ResponseException( PATH_NO_MATCHED));
	}

	/**
	 *构建Request请求对象
	 */
	private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
		
		HttpHeaders headers = fullHttpRequest.headers();
		//	从header头获取必须要传入的关键属性 uniqueId
		String uniqueId = headers.get(GatewayConst.UNIQUE_ID);
		
		String host = headers.get(HttpHeaderNames.HOST);
		HttpMethod method = fullHttpRequest.method();
		String uri = fullHttpRequest.uri();
		String clientIp = getClientIp(ctx, fullHttpRequest);
		String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
		Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

		GatewayRequest gatewayRequest = new GatewayRequest(uniqueId,
				charset,
				clientIp,
				host, 
				uri, 
				method,
				contentType,
				headers,
				fullHttpRequest);
		
		return gatewayRequest;
	}
	
	/**
	 * 获取客户端ip
	 */
	private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		//request.header 是一个map类型的数据结构
		/* 结构如下:
		request.headers().set(HttpHeaderNames.HOST, "localhost");
		request.headers().set(HttpHeaderNames.ACCEPT, "application/json");
		request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
		* */
		String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
		
		String clientIp = null;
		if(StringUtils.isNotEmpty(xForwardedValue)) {
			List<String> values = Arrays.asList(xForwardedValue.split(", "));
			if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
				clientIp = values.get(0);
			}
		}
		if(clientIp == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
			clientIp = inetSocketAddress.getAddress().getHostAddress();
		}
		return clientIp;
	}


}
