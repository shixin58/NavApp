package com.passion.navapp;

import android.app.Application;

import com.passion.libnetwork.ApiService;

public class JokeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://192.168.1.3:8080/serverdemo", null);
    }
}
