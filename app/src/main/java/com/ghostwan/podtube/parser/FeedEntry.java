package com.ghostwan.podtube.parser;

import org.simpleframework.xml.*;

import java.util.Date;

/**
 * Created by erwan on 06/05/2017.
 */
@Root(name = "entry", strict=false)
public class FeedEntry {

    @Element
    public String id;

    @Element
    @Namespace(prefix="yt", reference="http://www.youtube.com/xml/schemas/2015")
    public String videoId;

    @Element
    @Namespace(prefix="yt", reference="http://www.youtube.com/xml/schemas/2015")
    public String channelId;

    @Element
    public String title;

    @Path("link")
    @Attribute(name = "href")
    public String url;

    @Element
    public Author author;

    @Element
    public Date published;

    @Element
    public Date updated;

    @Element(name = "group")
    public MediaMetadata mediaMetadata;

    @Override
    public String toString() {
        return "FeedEntry{ \n" +
                "id='" + id + "'\n" +
                "videoId='" + videoId + "'\n" +
                "channelId='" + channelId + "'\n" +
                "title='" + title + "'\n" +
                "url='" + url + "'\n" +
                "author=" + author + "\n" +
                "published=" + published + "\n" +
                "updated=" + updated + "\n" +
                "mediaMetadata=" + mediaMetadata + "\n" +
                "}\n";
    }
}
