package com.ghostwan.podtube.parser;

import org.simpleframework.xml.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by erwan on 06/05/2017.
 */

@Root(name="feed", strict=false)
@Namespace(prefix="yt", reference="http://www.youtube.com/xml/schemas/2015")
public class Feed {

    @Path("link")
    @Attribute(name = "href")
    public String url;

    @Element(required = false)
    public String playlistId;

    @Element(required = false)
    public String channelId;

    @Element
    public String title;

    @Element
    public Author author;

    @Element
    public Date published;

    @ElementList(inline=true)
    public List<FeedEntry> entries = new ArrayList<>();


    @Override
    public String toString() {
        return "Feed{ \n" +
                "url='" + url + "'\n" +
                "playlistId='" + playlistId + "'\n" +
                "channelId='" + channelId + "'\n" +
                "title='" + title + "'\n" +
                "author=" + author + "\n" +
                "published=" + published + "\n" +
                "entries=" + entries + "\n" +
                "}\n";
    }
}

