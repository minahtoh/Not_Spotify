package com.example.notspotify.exoplayer.callbacks

import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.notspotify.exoplayer.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.material.snackbar.Snackbar

class MusicPlayerEventListener(
    private val musicService: MusicService
):Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY){
           musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService,"Unknown Error Occured",Toast.LENGTH_LONG).show()
    }
}