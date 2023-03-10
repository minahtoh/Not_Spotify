package com.example.notspotify.exoplayer


import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.notspotify.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.notspotify.exoplayer.callbacks.MusicPlayerEventListener
import com.example.notspotify.exoplayer.callbacks.MusicPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

const val MEDIA_ROOT_ID = "root_id"
@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {
    @Inject
    lateinit var dataSourceFactory: DataSource.Factory
    @Inject
    lateinit var exoPlayer: ExoPlayer
    @Inject
    lateinit var musicSource: MusicSource

    private lateinit var musicNotificationManager:MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = MainScope()


    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    var isForegroundService = false
    private var currentPlayingSong: MediaMetadataCompat? = null
    var isPlayerInitialized = false

    private lateinit var musicPlayerEventListener :MusicPlayerEventListener

    companion object{
        var currentSongDuration = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            musicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }
        mediaSession = MediaSessionCompat(this,"MediaService").apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
            currentSongDuration = exoPlayer.duration

        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource){
            currentPlayingSong = it
            preparePlayer(musicSource.songs, it, true)

        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.release()
        exoPlayer.removeListener(musicPlayerEventListener)
    }


    inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return musicSource.songs[windowIndex].description
        }

    }
    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay:MediaMetadataCompat?,
        playNow:Boolean
    ){
        val songIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
     MainScope().launch {
         exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(songIndex,0L)
        exoPlayer.playWhenReady = playNow
     }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
       return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID -> {
                val resultSent = musicSource.whenReady {
                    if (it){
                      result.sendResult(musicSource.asMediaItems())
                        if(!isPlayerInitialized && musicSource.songs.isNotEmpty()){
                            preparePlayer(musicSource.songs,musicSource.songs[0],false)
                            isPlayerInitialized = true
                        }
                    }else{
                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                        result.sendResult(null)
                    }
                }
                if (!resultSent){
                    result.detach()
                }
            }
        }
    }
}