package com.carusto.ReactNativePjSip.dto;

import com.facebook.react.bridge.ReadableMap;
import com.google.gson.Gson;

public class CallSettingsDTO {
    private Integer audioCount;
    private Integer videoCount;
    private Integer flag;
    private Integer requestKeyframeMethod;

    public Integer getAudioCount() {
        return audioCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public Integer getFlag() {
        return flag;
    }

    public Integer getRequestKeyframeMethod() {
        return requestKeyframeMethod;
    }

    public void setAudioCount(Integer audioCount) {
        this.audioCount = audioCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public void setRequestKeyframeMethod(Integer requestKeyframeMethod) {
        this.requestKeyframeMethod = requestKeyframeMethod;
    }

    public String toJson () {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static CallSettingsDTO fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CallSettingsDTO.class);
    }

    public static CallSettingsDTO fromReadableMap(ReadableMap data) {
        CallSettingsDTO result = new CallSettingsDTO();

        if (data.hasKey("audioCount")) {
            result.setAudioCount(data.getInt("audioCount"));
        }
        if (data.hasKey("videoCount")) {
            result.setVideoCount(data.getInt("videoCount"));
        }
        if (data.hasKey("flag")) {
            result.setFlag(data.getInt("flag"));
        }
        if (data.hasKey("requestKeyframeMethod")) {
            result.setRequestKeyframeMethod(data.getInt("requestKeyframeMethod"));
        }

        return result;
    }

}
