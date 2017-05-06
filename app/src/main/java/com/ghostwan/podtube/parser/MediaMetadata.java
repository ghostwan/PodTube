package com.ghostwan.podtube.parser;

import org.simpleframework.xml.*;

/**
 * Created by erwan on 06/05/2017.
 */
@Namespace(prefix="mediaMetadata", reference="http://search.yahoo.com/mrss/")
@Root(name = "group", strict=false)
public class MediaMetadata {

    @Path("thumbnail")
    @Attribute(name = "url")
    public String thumbnailUrl;

    @Element
    public String description;

    @Path("community/starRating")
    @Attribute(name = "average")
    public Float rating;

    @Path("community/statistics")
    @Attribute(name = "views")
    public Integer views;

    @Override
    public String toString() {
        return "MediaMetadata{ \n" +
                "thumbnailUrl='" + thumbnailUrl + "'\n" +
                "description='" + description + "'\n" +
                "rating=" + rating + "\n" +
                "views=" + views + "\n" +
                "}\n";
    }
}
