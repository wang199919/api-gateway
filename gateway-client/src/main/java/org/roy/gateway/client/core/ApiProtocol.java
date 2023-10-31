package org.roy.gateway.client.core;

/**
 * @author: roy
 * @date: 2023/10/26 15:39
 * @description:
 */
public enum ApiProtocol {
    HTTP("http","http协议"),DUBBD("dubbe","dubbe协议");
    public  String code;

    public  String desc;

    ApiProtocol(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
