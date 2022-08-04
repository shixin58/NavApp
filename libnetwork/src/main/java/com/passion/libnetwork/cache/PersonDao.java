package com.passion.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface PersonDao {
    @Insert
    void insert(Person person);
}
