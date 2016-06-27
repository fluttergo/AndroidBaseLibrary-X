package org.xbase.android.log;


public class LogConfig {

    /**
     * 可以使TAG, 也可以使包名， 支持通配符: com.zz.* 格式
     * 表示com.zz下面的所有子包， 都是用该配置
     */
    public String filter;
    
    public LogLevel logLevel;
}
