package com.appmsg.front.appmensajeriafront.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WallpaperProvider {
    public static List<String> getWallpaperUrls() {
        List<String> urls = new ArrayList<>();
        String[] files = {"wp1.jpg", "wp2.jpg", "wp3.jpg"};
        for (String name : files) {
            URL url = WallpaperProvider.class.getResource("/com/appmsg/front/appmensajeriafront/wallpapers/" + name);
            if (url != null) {
                urls.add(url.toExternalForm());
            }
        }
        return urls;
    }
}
