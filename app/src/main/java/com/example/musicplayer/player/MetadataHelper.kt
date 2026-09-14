package com.example.musicplayer.player

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
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
        newTitle: String?,
        newArtist: String?,
        newAlbum: String?
    ): Boolean {
        return try {
            // Because Jaudiotagger requires a java.io.File, we must copy the SAF stream
            // to a temporary file, modify it, and then write it back.
            val tempFile = File(context.cacheDir, "temp_audio_file")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val audioFile = AudioFileIO.read(tempFile)
            val tag = audioFile.tagOrCreateAndSetDefault

            newTitle?.let { tag.setField(FieldKey.TITLE, it) }
            newArtist?.let { tag.setField(FieldKey.ARTIST, it) }
            newAlbum?.let { tag.setField(FieldKey.ALBUM, it) }

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
