package com.passion.navapp.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.passion.libcommon.AppGlobals;
import com.passion.navapp.model.BottomBar;
import com.passion.navapp.model.Destination;
import com.passion.navapp.model.SofaTabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;

public class AppConfig {
    // 用于首页，除非杀进程，不会被销毁。
    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottomBar;
    private static SofaTabs sSofaTabs;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null) {
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>(){}.getType());
        }
        return sDestConfig;
    }

    public static BottomBar getBottomBar() {
        if (sBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }

    public static SofaTabs getSofaTabs() {
        if (sSofaTabs == null) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTabs = JSON.parseObject(content, SofaTabs.class);
            Collections.sort(sSofaTabs.tabs, (o1, o2) -> o1.index - o2.index);
        }
        return sSofaTabs;
    }

    private static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = assets.open(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
