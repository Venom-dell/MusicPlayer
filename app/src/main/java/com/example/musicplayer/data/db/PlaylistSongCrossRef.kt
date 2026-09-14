package com.example.musicplayer.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songUri"],
    indices = [Index("songUri")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songUri: String
)
