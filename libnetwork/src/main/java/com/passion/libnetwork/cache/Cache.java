package com.passion.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "cache"
        /*, foreignKeys = {@ForeignKey(entity = Person.class, parentColumns = "id", childColumns = "key", onDelete = ForeignKey.RESTRICT)}
        , indices = {@Index(value = {"key"})}*/)
public class Cache implements Serializable {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String key;

//    @ColumnInfo(name = "_data")
    public byte[] data;

//    @Embedded
//    public Worker user;

//    @TypeConverters(value = {DateConverter.class})
//    public Date date;
}
