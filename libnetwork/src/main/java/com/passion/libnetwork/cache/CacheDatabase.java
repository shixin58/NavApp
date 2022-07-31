package com.passion.libnetwork.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.passion.libcommon.AppGlobals;

@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {
    private static final CacheDatabase database;

    static {
        // 创建内存数据库。进程被杀，不保存数据
//        Room.inMemoryDatabaseBuilder()
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "ppjoke_db")
                .allowMainThreadQueries()
                // 设置数据库创建和打开的回调
//                .addCallback()
                // 设置查询的线程池
//                .setQueryExecutor()
//                .openHelperFactory()
                // 设置日志模式
//                .setJournalMode()
//                .fallbackToDestructiveMigration()
                // 数据库升级异常按指定版本回滚
//                .fallbackToDestructiveMigrationFrom()
//                .addMigrations(CacheDatabase.sMigration)
                .build();
    }

    // 只有在此声明，才会生成Dao实现类
    public abstract CacheDao getCache();

    public static CacheDatabase get() {
        return database;
    }

//    static Migration sMigration = new Migration(1, 3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//            database.execSQL("alter table teacher add column teacher_age INTEGER NOT NULL default 0");
//        }
//    };
}
