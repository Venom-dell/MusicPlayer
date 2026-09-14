package com.example.musicplayer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = false) val uriString: String,
    val title: String,
    val artist: String,
    val album: String
)
