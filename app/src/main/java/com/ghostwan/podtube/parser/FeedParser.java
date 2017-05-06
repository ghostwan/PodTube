package com.ghostwan.podtube.parser;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by erwan on 06/05/2017.
 */
public class FeedParser {

    public static Feed parse(URL url) throws Exception {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        RegistryMatcher registryMatcher = new RegistryMatcher();
        registryMatcher.bind(Date.class, new AtomDateFormatTransformer());
        Serializer serializer = new Persister(registryMatcher);
        return serializer.read(Feed.class, urlConnection.getInputStream());
    }


}
