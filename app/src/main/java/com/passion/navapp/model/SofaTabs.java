package com.passion.navapp.model;

import java.util.List;

/**
 * activeSize : 16
 * normalSize : 14
 * activeColor : #ED7282
 * normalColor : #666666
 * select : 0
 * tabGravity : 0
 * tabs : [{"title":"图片","index":0,"tag":"pics","enable":true},{"title":"视频","index":1,"tag":"video","enable":true},{"title":"文本","index":1,"tag":"text","enable":true}]
 */
public class SofaTabs {
    public int activeSize;
    public int normalSize;
    public String activeColor;
    public String normalColor;
    public int select;
    public int tabGravity;
    public List<Tab> tabs;

    /**
     * title : 图片
     * index : 0
     * tag : pics
     * enable : true
     */
    public static class Tab {
        public String title;
        public int index;
        public String tag;
        public boolean enable;
    }
}
