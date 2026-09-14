package com.example.musicplayer.player

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataHelper @Inject constructor(
    private val context: Context
) {

    fun modifyMetadata(
        uri: Uri,
        title: String?,
        artist: String?,
        album: String?
    ): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri) ?: return false
            val fileName = documentFile.name ?: "temp_audio_file"

            // We must keep the extension so jaudiotagger knows how to parse it.
            val tempFile = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val audioFile = AudioFileIO.read(tempFile)
            val tag = audioFile.tagOrCreateAndSetDefault

            title?.let { if (it.isNotBlank()) tag.setField(FieldKey.TITLE, it) }
            artist?.let { if (it.isNotBlank()) tag.setField(FieldKey.ARTIST, it) }
            album?.let { if (it.isNotBlank()) tag.setField(FieldKey.ALBUM, it) }

            audioFile.commit()

            // Write back to SAF URI
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                tempFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            tempFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteFile(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.delete() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
