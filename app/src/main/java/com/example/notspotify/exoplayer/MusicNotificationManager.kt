package com.example.notspotify.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.notspotify.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_HIGH

const val NOTIFICATION_ID = "Notification music"
const val NOTIFICATION_CHANNEL_ID = 1
class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback:()->Unit
) {
    private val notificationManager:PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(context, NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_ID)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .setSmallIconResourceId(R.drawable.ic_baseline_music)
            .setChannelImportance(IMPORTANCE_HIGH)
            .build()
    }

    fun showNotification(player:Player){
        notificationManager.setPlayer(player)
    }


    inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ): PlayerNotificationManager.MediaDescriptionAdapter{

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
           return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
           return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
           Glide.with(context).asBitmap()
               .load(mediaController.metadata.description.iconUri)
               .into(object:CustomTarget<Bitmap>(){
                   override fun onResourceReady(
                       resource: Bitmap,
                       transition: Transition<in Bitmap>?
                   ) {
                       callback.onBitmap(resource)
                   }

                   override fun onLoadCleared(placeholder: Drawable?) = Unit

               })
            return null
        }

    }
}