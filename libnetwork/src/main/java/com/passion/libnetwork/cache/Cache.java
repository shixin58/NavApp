package com.passion.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cache")
public class Cache {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String key;

//    @ColumnInfo(name = "_data")
    public byte[] data;
}
