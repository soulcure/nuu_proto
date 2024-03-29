package com.nuu.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * json字符串 和对象互换 工具类 依赖Gson包
 *
 * @see Gson
 */
public class GsonUtil {

    private static final String TAG = GsonUtil.class.getSimpleName();

    /**
     * 将json字符串转换成对象
     *
     * @param json
     * @param cls
     * @return
     */
    public static <T> T parse(String json, Class<T> cls) {
        Gson gson = new Gson();
        T t;
        try {
            t = gson.fromJson(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }

        return t;
    }


    /**
     * 使用Gson进行解析 List<Person>
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> parseList(String json, Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(json, new TypeToken<List<T>>() {
            }.getType());

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return list;
    }


    public static <T> List<T> parseList1(String json, Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (JsonElement jsonElement : array) {
                list.add(gson.fromJson(jsonElement, cls));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return list;
    }


    public static <T> List<T> parseToArray(String s, Class<T[]> clazz) {
        T[] arr = new Gson().fromJson(s, clazz);
        return Arrays.asList(arr); //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }


    /**
     * 将对象转成json字符串
     *
     * @param o
     * @return
     */
    public static String format(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    /**
     * 将对象转成json字符串 并使用url编码
     *
     * @param o
     * @return
     */
    public static String formatURLString(Object o) {
        try {
            return URLEncoder.encode(format(o), "utf-8");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
