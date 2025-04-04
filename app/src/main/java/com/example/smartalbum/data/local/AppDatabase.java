package com.example.smartalbum.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Photo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();
}
