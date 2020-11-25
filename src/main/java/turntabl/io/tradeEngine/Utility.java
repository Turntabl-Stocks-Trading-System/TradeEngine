package turntabl.io.tradeEngine;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utility {
    public static <T> T convertToObject(String data, Class<T> type){
        ObjectMapper objectMapper = new ObjectMapper();
        T t = null;

        try {
            t = objectMapper.readValue(data, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;
    }


    public static <T> String convertToString(T t){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String str = null;

        try {
            str = objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return str;

    }
}
