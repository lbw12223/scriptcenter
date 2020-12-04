package com.jd.bdp.datadev.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.model.Script;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AjaxFastJSONUtil {
    private static Log logger = LogFactory.getLog(AjaxFastJSONUtil.class);
    private static SerializeConfig DEFAULT_CONFIG = DateConfig.instance;

    /**
     * 将PageResultDTO对象转成JqGrid可以使用的JSON串
     *
     * @param pageResultDTO
     * @return
     */
    public static JSONObject gridJson(PageResultDTO pageResultDTO) {

        JSONObject json = new JSONObject();
        if (pageResultDTO == null || !pageResultDTO.isSuccess()) {
            json.put("records", 0);
            json.put("page", 1);
            json.put("message", pageResultDTO == null ? "PageResultDTO 对象为空" : pageResultDTO.getMessage());
            json.put("success", false);
            json.put("rows", ArrayUtils.EMPTY_OBJECT_ARRAY);
            return json;
        }
        json.put("success", true);
        int pageNum = (int) Math.ceil((double) pageResultDTO.getRecords() / (double) pageResultDTO.getLimit());
        json.put("total", pageNum);
        json.put("page", pageResultDTO.getPage());
        json.put("records", pageResultDTO.getRecords());
        List dataList = pageResultDTO.getRows();
        if (dataList != null && dataList.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (Object item : dataList) {
                Class c = item.getClass();
                if (c == int.class || c == Integer.class ||
                        c == long.class || c == Long.class ||
                        c == float.class || c == Float.class ||
                        c == double.class || c == Double.class ||
                        c == boolean.class || c == Boolean.class ||
                        c == byte.class || c == Byte.class ||
                        c == char.class || c == Byte.class ||
                        c == short.class || c == Short.class ||
                        c == String.class) {
                    jsonArray.add(item);
                } else {
                    jsonArray.add(JSONObject.parseObject(JSONObject.toJSONString(item,DateConfig.instance)));
                }
            }
            json.put("rows", jsonArray);
        } else {
            json.put("rows", ArrayUtils.EMPTY_OBJECT_ARRAY);
        }
        return json;
    }

    public static void main(String[] args) {
        DataDevScriptFile script=new DataDevScriptFile();
        script.setCreated(new Date());
        System.out.println(JSONObject.toJSON(script,DateConfig.instance));
    }


}

class FastJsonCustomerDateJsonSerializer implements ObjectSerializer {
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        out.write(StringDateUtils.DateToSpecialString(object));
    }
}
class StringDateUtils{
    public static String DateToSpecialString(Object object){
        if(null == object ){
            return "" ;
        }else{
            if(object instanceof java.util.Date||object instanceof java.sql.Date||object instanceof Timestamp){
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return df.format(object);
            }
        }
        return object.toString();
    }
}
  class  DateConfig extends SerializeConfig {
    private DateConfig(){
        super();
        put(Date.class,new FastJsonCustomerDateJsonSerializer());
        put(java.sql.Date.class,new FastJsonCustomerDateJsonSerializer());
        put(Timestamp.class,new FastJsonCustomerDateJsonSerializer());
    }
    public static DateConfig instance = new DateConfig();
}

