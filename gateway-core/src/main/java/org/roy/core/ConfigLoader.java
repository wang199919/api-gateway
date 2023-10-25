package org.roy.core;

import lombok.extern.slf4j.Slf4j;
import org.roy.common.util.PropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author: roy
 * @date: 2023/10/23 15:12
 * @description:
 */
@Slf4j
public class ConfigLoader {

    private  static  final  String CONFIG_FILE="gateway.properties";

    private static final String ENV_PREFIX="GATEWAY_";

    private  static  final String JVM_PREFIX="gateway.";

    //饿汉式单例模式
    private  static  final  ConfigLoader INSTANCE=new ConfigLoader();

    private  ConfigLoader(){};

    public   static ConfigLoader getInstance(){
        return  INSTANCE;
    }

    private Config config;

    public  static  Config getConfig(){
        return INSTANCE.config;
    }

    /**
     * 运行参数 -> jvm 参数 ->环境变量 ->配置文件 -> 配置对象默认值
     * @param args
     * @return
     */
    public  Config load(String args[]){
        //配置文件默认值
        config =new Config();
        //配置文件
        loadFromConfigFile();

        //环境变量
        loadFromEnv();

        //jvm参数
        loadFromJvm();

        //运行参数
        loadFromArgs(args);
        return config;
    }

    private void loadFromArgs(String [] args) {
        if(args!=null&&args.length>0){
            Properties properties=new Properties();
            for (String arg:args){
                if (arg.startsWith("--")&&arg.contains("=")){
                    properties.put(arg.substring(2,arg.indexOf("=")),arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtils .properties2Object(properties,config);
        }
    }

    private void loadFromJvm() {
        Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties,config,JVM_PREFIX);
    }

    private void loadFromEnv() {
        Map<String,String> env=System.getenv();
        Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties,config,ENV_PREFIX);
    }

    private void loadFromConfigFile() {
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if(inputStream != null){
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                PropertiesUtils.properties2Object(properties,config);
            } catch (IOException e) {
                log.warn("load config file { } error",e);
            }finally {
                if (inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
