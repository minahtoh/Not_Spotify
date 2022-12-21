package com.example.notspotify.di

import android.content.Context
import com.example.notspotify.data.remote.MusicDatabase
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = MusicDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes()= AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(@ApplicationContext context:Context, audioAttributes: AudioAttributes)=
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(audioAttributes,true)
            setHandleAudioBecomingNoisy(false)
        }


   /* @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context)=
        DefaultDataSource.Factory(context)*/

    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context): DataSource.Factory {
        return DefaultDataSource.Factory(context)
    }


}