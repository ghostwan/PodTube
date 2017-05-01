package com.ghostwan.podtube.feed;

/**
 * Created by erwan on 28/03/2017.
 */

public class FeedInfo {

    private String name;
    private String url;

    public FeedInfo(String name, String url) {
        this.name = name;
        this.url = url;
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
