package com.ghostwan.podtube.feed;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by erwan on 28/03/2017.
 */

public class FeedInfo {

    public static String SETTING_FOLDER = "setting_folder";
    private String name;
    private String url;
    private Map<String, String> settings;

    public FeedInfo(String name, String url) {
        this.name = name;
        this.url = url;
        this.settings = new HashMap<>();
    }

    public FeedInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getSettings() {
        if (settings == null)
            settings = new HashMap<>();
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public boolean isSettingSet(String key) {
        return getSettings().containsKey(key);
    }

    public String getSettingValue(String key) {
        return getSettings().get(key);
    }

    public void setSettingValue(String key, String value) {
        getSettings().put(key, value);
    }

    public void deleteSetting(String key) {
        getSettings().remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedInfo feedInfo = (FeedInfo) o;

        if (!name.equals(feedInfo.name)) return false;
        return url.equals(feedInfo.url);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }
}
