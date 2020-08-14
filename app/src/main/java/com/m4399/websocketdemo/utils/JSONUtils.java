package com.m4399.websocketdemo.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Project Name: WebsocketDemo
 * File Name:    JSONUtils.java
 * ClassName:    JSONUtils
 *
 * Description: JSON工具类.
 *
 * @author LDC
 * @date 2020年08月14日 9:14
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class JSONUtils
{
    public static JSONObject parseJSONObjectFromString(String json)
    {
        JSONObject jsonObj = new JSONObject();
        if (!TextUtils.isEmpty(json))
        {
            try
            {
                jsonObj = new JSONObject(json);
            }
            catch (JSONException e)
            {
                e.printStackTrace();

                log("Exception occured when parsing the json:" + json);
            }
        }

        return jsonObj;
    }

    public static int getInt(String key, JSONObject object)
    {
        int value = -1;

        if (object == null)
        {
            return value;
        }

        try
        {
            if (object.has(key))
            {
                Object obj = object.get(key);

                if (obj instanceof String)
                {
                    value = Integer.valueOf(((String) obj).trim());
                }
                else if (obj instanceof Integer)
                {
                    value = (int) obj;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();

            log("JSONUtils.getString() exception => " + e.toString());
        }

        return value;
    }

    public static String getString(String key, JSONObject object)
    {
        String value = "";

        if (object == null)
        {
            return value;
        }

        try
        {
            if (object.has(key))
            {
                value = object.getString(key);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();

            log("JSONUtils.getString() exception => " + e.toString());
        }

        return value;
    }

    public static void log(String msg)
    {
        Log.d("Ricardo", msg);
    }
}
