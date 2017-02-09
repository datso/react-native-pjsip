package com.carusto.ReactNativePjSip.utils;

import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArgumentUtils {

    public static Object fromJson(String json) {
        return fromJson(new JsonParser().parse(json));
    }

    private static Object fromJson(JsonElement el) {
        if (el instanceof JsonObject) {
            return fromJsonObject(el.getAsJsonObject());
        } else if (el instanceof JsonArray) {
            return fromJsonArray(el.getAsJsonArray());
        } else {
            return fromJsonPrimitive(el.getAsJsonPrimitive());
        }
    }

    private static WritableMap fromJsonObject(JsonObject object) {
        WritableMap result = new WritableNativeMap();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Object value = fromJson(entry.getValue());

            if (value instanceof WritableMap) {
                result.putMap(entry.getKey(), (WritableMap) value);
            } else if (value instanceof WritableArray) {
                result.putArray(entry.getKey(), (WritableArray) value);
            } else if (value instanceof String) {
                result.putString(entry.getKey(), (String) value);
            } else if (value instanceof LazilyParsedNumber) {
                result.putInt(entry.getKey(), ((LazilyParsedNumber) value).intValue());
            } else if (value instanceof Integer) {
                result.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Double) {
                result.putDouble(entry.getKey(), (Double) value);
            } else if (value instanceof Boolean) {
                result.putBoolean(entry.getKey(), (Boolean) value);
            } else {
                Log.d("ArgumentUtils", "Unknown type: " + value.getClass().getName());
                result.putNull(entry.getKey());
            }
        }

        return result;
    }

    private static Object fromJsonPrimitive(JsonPrimitive object) {
        if (object.isString()) {
            return object.getAsString();
        } else if (object.isNumber()) {
            return object.getAsNumber();
        } else if (object.isBoolean()) {
            return object.getAsBoolean();
        }

        return null;
    }

    private static WritableArray fromJsonArray(JsonArray arr) {
        WritableArray result = new WritableNativeArray();

        for (JsonElement el : arr) {
            Object item = fromJson(el);

            if (item instanceof WritableMap) {
                result.pushMap((WritableMap) item);
            } else if (item instanceof WritableArray) {
                result.pushArray((WritableArray) item);
            } else if (item instanceof String) {
                result.pushString((String) item);
            } else if (item instanceof LazilyParsedNumber) {
                result.pushInt(((LazilyParsedNumber) item).intValue());
            } else if (item instanceof Integer) {
                result.pushInt((Integer) item);
            } else if (item instanceof Double) {
                result.pushDouble((Double) item);
            } else if (item instanceof Boolean) {
                result.pushBoolean((Boolean) item);
            } else {

                Log.d("ArgumentUtils", "Unknown type: " + item.getClass().getName());

                result.pushNull();
            }
        }

        return result;
    }


    public static String dumpIntentExtraParameters(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return "empty extras";
        }

        Set<String> keys = intent.getExtras().keySet();
        Map<String, Object> data = new HashMap<>(keys.size());

        for (String key : keys) {
            data.put(key, intent.getExtras().get(key));
        }

        Gson gson = new Gson();
        return gson.toJson(data);
    }


}
