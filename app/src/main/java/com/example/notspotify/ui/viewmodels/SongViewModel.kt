package com.example.notspotify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notspotify.exoplayer.MusicService
import com.example.notspotify.exoplayer.MusicServiceConnection
import com.example.notspotify.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
): ViewModel() {
    private val playbackState = musicServiceConnection.playbackState
    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration : LiveData<Long> = _curSongDuration
    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition : LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }


    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch {
            while (true){
                val position = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != position){
                    _curPlayerPosition.postValue(position!!)
                    _curSongDuration.postValue(MusicService.currentSongDuration)
                }
            }
        }
    }
}