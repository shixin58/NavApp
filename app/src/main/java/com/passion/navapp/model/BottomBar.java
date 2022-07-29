package com.passion.navapp.model;

import java.util.List;

public class BottomBar {
    public String activeColor;
    public String inactiveColor;
    public int selectTab;

    public List<TabBean> tabs;

    public static class TabBean {
        public int size;
        public boolean enable;
        public int index;
        public String tintColor;
        public String pageUrl;
        public String title;
    }
}
