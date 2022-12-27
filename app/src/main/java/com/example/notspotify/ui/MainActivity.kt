package com.example.notspotify.ui

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.notspotify.R
import com.example.notspotify.adapters.SwipeAdapter
import com.example.notspotify.data.models.Track
import com.example.notspotify.databinding.ActivityMainBinding
import com.example.notspotify.exoplayer.isPlaying
import com.example.notspotify.other.Status
import com.example.notspotify.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel : MainViewModel by viewModels()
    @Inject
    lateinit var glide: RequestManager
    @Inject
    lateinit var swipeAdapter: SwipeAdapter
    private lateinit var binding: ActivityMainBinding
    private var currentPlaying : Track? = null
    private var playbackState : PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()
        val navHostFragment = R.id.navHostFragment


        binding.vpSong.apply {
            adapter = swipeAdapter
            registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (playbackState?.isPlaying == true){
                        mainViewModel.playOrToggleSong(swipeAdapter.currentList[position])
                    }  else{
                        currentPlaying = swipeAdapter.currentList[position]
                    }
                }
            })
        }
        swipeAdapter.setOnItemClickListener {
           findNavController(navHostFragment).navigate(R.id.globalActionToSongFragment)


        }

        binding.ivPlayPause.setOnClickListener {
            currentPlaying?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        findNavController(navHostFragment).addOnDestinationChangedListener{_,destination,_ ->
            when(destination.id){
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> hideBottomBar()
            }
        }

    }
    private fun hideBottomBar(){
        binding.apply {
            ivCurSongImage.visibility = View.INVISIBLE
            vpSong.visibility = View.INVISIBLE
            ivPlayPause.visibility = View.INVISIBLE
        }
    }
    private fun showBottomBar(){
        binding.apply {
            ivCurSongImage.visibility = View.VISIBLE
            vpSong.visibility = View.VISIBLE
            ivPlayPause.visibility = View.VISIBLE
        }
    }

    private fun switchViewPager(song:Track){
        val newSongIndex = swipeAdapter.currentList.indexOf(song)
        if(newSongIndex != -1){
            binding.vpSong.currentItem = newSongIndex
            currentPlaying = song
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let { result->
            when(result.status){
                Status.SUCCESS -> {
                    result.data?.let { songList->
                       swipeAdapter.submitList(songList)
                        if (songList.isNotEmpty()){
                            glide.load((currentPlaying ?: songList[0]).image_url).into(binding.ivCurSongImage)
                        }
                        switchViewPager(currentPlaying?: return@observe)
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> Unit
            }
            }
        }

        mainViewModel.currPlayingSong.observe(this){
            if(it == null) return@observe
            currentPlaying = it.toSong()
            glide.load(currentPlaying?.image_url).into(binding.ivCurSongImage)
            switchViewPager(currentPlaying?: return@observe)
        }

        mainViewModel.playbackState.observe(this){
            playbackState = it
            if (playbackState?.isPlaying == true){
                binding.ivPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
            } else{
                binding.ivPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow)
            }
        }

        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){
                    Status.ERROR -> Snackbar.make(
                        binding.rootLayout,
                        result.message ?: "An Unknown Error Occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Status.SUCCESS -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){
                    Status.ERROR -> Snackbar.make(
                        binding.rootLayout,
                        result.message ?: "An Unknown Error Occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Status.SUCCESS -> Unit
                    Status.LOADING -> Unit
                }
            }
        }
    }


}

fun MediaMetadataCompat.toSong():Track?{
    return description?.let {
        Track(
            it.title.toString(),
            it.subtitle.toString(),
            "",
            it.mediaId?: "",
            it.iconUri.toString(),
            it.mediaUri.toString()
        )
    }
}