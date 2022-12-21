package com.example.notspotify.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.notspotify.R
import com.example.notspotify.adapters.SwipeAdapter
import com.example.notspotify.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeAdapter() = SwipeAdapter()

    @Singleton
    @Provides
    fun provideGlideInstance(@ApplicationContext context: Context)=
         Glide.with(context).applyDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )

}