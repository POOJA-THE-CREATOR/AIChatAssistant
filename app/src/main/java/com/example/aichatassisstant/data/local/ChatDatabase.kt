package com.example.aichatassisstant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aichatassisstant.data.local.dao.ChatMessageDao
import com.example.aichatassisstant.data.local.entity.ChatMessageEntity

@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao
}
