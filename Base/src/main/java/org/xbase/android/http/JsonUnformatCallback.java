package org.xbase.android.http;


import org.json.JSONObject;

/**
 * 对应服务端接口风格的json解析器
 * @author Ge Liang
 *
 * @param <T> bean
 */
public abstract class JsonUnformatCallback<T> extends AbstractUnformatCallback<T> {
    @Override
    protected String unFormatContent(String result) throws Exception  {
        JSONObject js = new JSONObject(result);
        final int status = js.optInt("status");
        if (status == 0) {
            return js.opt("data").toString();
        } else {
            throw new Exception("status!=0:" + result);
        }
    }
}
