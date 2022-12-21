package com.example.notspotify.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.notspotify.data.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
){
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = SongState.STATE_INITIALIZING
        val allSongs = musicDatabase.getAllTracks()
        songs = allSongs.map { song->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.artiste)
                .putString(METADATA_KEY_ALBUM, song.album)
                .putString(METADATA_KEY_DISPLAY_TITLE,song.title)
                .putString(METADATA_KEY_ALBUM_ART_URI,song.image_url)
                .putString(METADATA_KEY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,song.image_url)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artiste)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.artiste)
                .putString(METADATA_KEY_MEDIA_URI,song.song_url)
                .putString(METADATA_KEY_MEDIA_ID,song.media_Id)
                .build()
        }
        state = SongState.STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DataSource.Factory):ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach{
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(it.getString(METADATA_KEY_MEDIA_URI).toUri()))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map{song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()


    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state : SongState = SongState.STATE_CREATED
        set(value) {
            if(value == SongState.STATE_INITIALIZED|| value == SongState.STATE_ERROR){
                synchronized(onReadyListeners){
                    field = value
                    onReadyListeners.forEach {
                        it(state == SongState.STATE_INITIALIZED)
                    }
                }
            }else{
                field = value
            }
        }

    fun whenReady(action:(Boolean)->Unit):Boolean{
        if (state == SongState.STATE_CREATED||state == SongState.STATE_INITIALIZING){
            onReadyListeners += action
            return false
        }else{
            action(state == SongState.STATE_INITIALIZED)
            return true
        }
    }

}

enum class SongState{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}