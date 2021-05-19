package com.wy.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy.exception.JsonErrorException;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * wangyong
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转成json
     * @param data 对象
     * @param <T>
     * @return
     */
    public static <T> String toJson(T data){
        String json = null;
        try {
            json = OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("convert to json error:" ,e);
            throw JsonErrorException.CONVERT_JSON_ERROR.get();
        }
        return json;
    }


    /**
     * json转成对象
     * @param json json字符串
     * @param clazz 对象类型
     * @param <T>
     * @return
     */
    public static <T> T toObject(String json, Class<T> clazz){
        T object = null;
        try {
            object = OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("convert to object error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ERROR.get();
        }
        return object;
    }


    /**
     * json转成对象
     * @param file 文件对象
     * @param clazz 对象类型
     * @param <T>
     * @return
     */
    public static <T> T toObject(File file, Class<T> clazz){
        T object = null;
        try {
            object = OBJECT_MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            log.error("convert to object error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ERROR.get();
        }
        return object;
    }


    /**
     * json转成对象
     * @param bytes bytes数组
     * @param clazz 对象类型
     * @param <T>
     * @return
     */
    public static <T> T toObject(byte[] bytes, Class<T> clazz){
        T object = null;
        try {
            object = OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("convert to object error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ERROR.get();
        }
        return object;
    }


    /**
     * json转成对象
     * @param in 数据流
     * @param clazz 对象类型
     * @param <T>
     * @return
     */
    public static <T> T toObject(InputStream in, Class<T> clazz){
        T object = null;
        try {
            object = OBJECT_MAPPER.readValue(in, clazz);
        } catch (IOException e) {
            log.error("convert to object error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ERROR.get();
        }
        return object;
    }


    /**
     * json转成制定类型
     * @param jsonArray json字符串
     * @param type 对象类型 new TypeReference<Map<String, Object>>   new TypeReference<List<Object>>
     * @param <T>
     * @return
     */
    public static <T> T toObjects(String jsonArray, TypeReference<T> type){
        T objects = null;
        try {
            objects = OBJECT_MAPPER.readValue(jsonArray, type);
        } catch (JsonProcessingException e) {
            log.error("convert to objects error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ERROR.get();
        }
        return objects;

    }

    /**
     * 将 JSON 数组转换为集合
     * @param jsonArray json数组
     * @return
     */
    public static <T> List<T> json2Array(String jsonArray,TypeReference<List<T>> reference) {
        try {
            List<T> arrays = OBJECT_MAPPER.readValue(jsonArray, reference);
            return arrays;
        } catch (JsonProcessingException e) {
            log.error("convert to objects array error: " ,e);
            throw JsonErrorException.CONVERT_OBJECT_ARRAY_ERROR.get();
        }
    }


    /**
     * 有bug 会生成hash结构的对象
     *
     * 将 JSON 数组转换为集合
     * @param jsonArray json数组
     * @param object 对象类型
     * @return
     */
    @Deprecated
    public static <T> List<T> json2list(String jsonArray, Class object){
        return toObjects(jsonArray, new TypeReference<List<T>>() {});
    }

}
