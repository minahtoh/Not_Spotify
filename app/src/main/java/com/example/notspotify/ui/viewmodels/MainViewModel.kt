package com.example.notspotify.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notspotify.data.models.Track
import com.example.notspotify.exoplayer.*
import com.example.notspotify.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
):ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Track>>>()
    val mediaItems : LiveData<Resource<List<Track>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currPlayingSong = musicServiceConnection.currPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                   Track(
                       it.description.title.toString(),
                       it.description.subtitle.toString(),
                       it.description.title.toString(),
                       it.mediaId!!,
                       it.description.iconUri.toString(),
                       it.description.mediaUri.toString()
                   )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }
    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }
    fun seekTo(position : Long){
        musicServiceConnection.transportControls.seekTo(position)
    }

    fun playOrToggleSong(mediaItem:Track, toggle:Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.media_Id ==
            currPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let { playstate->
                when{
                    playstate.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playstate.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.media_Id, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}