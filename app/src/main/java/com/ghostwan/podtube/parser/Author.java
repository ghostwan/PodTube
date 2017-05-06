package com.ghostwan.podtube.parser;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by erwan on 06/05/2017.
 */
@Root(name = "author")
public class Author {

    @Element
    private String name;

    @Element
    private String uri;

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
