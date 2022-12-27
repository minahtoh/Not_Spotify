package com.example.notspotify.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.notspotify.R
import com.example.notspotify.data.models.Track
import com.example.notspotify.databinding.FragmentSongBinding
import com.example.notspotify.exoplayer.isPlaying
import com.example.notspotify.other.Status
import com.example.notspotify.ui.toSong
import com.example.notspotify.ui.viewmodels.MainViewModel
import com.example.notspotify.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class SongFragment : Fragment() {
   private lateinit var binding: FragmentSongBinding
   @Inject
   lateinit var glide:RequestManager
   private lateinit var mainViewModel: MainViewModel
   private val songViewModel: SongViewModel by viewModels()
    private var currentPlayingSong : Track? = null
    private var playbackState : PlaybackStateCompat? = null
    private var toUpdateSeekBar = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSongBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        subscribeToObservers()

        binding.apply {
            ivPlayPauseDetail.setOnClickListener {
                currentPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }
            ivSkipPrevious.setOnClickListener {
                mainViewModel.skipToPreviousSong()
            }
            ivSkip.setOnClickListener {
                mainViewModel.skipToNextSong()
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p2){
                        setCurrentPlayerTimeToTextView(p1.toLong())
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    toUpdateSeekBar = false
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        mainViewModel.seekTo(it.progress.toLong())
                        toUpdateSeekBar = true
                    }
                }

            })
        }
    }



    private fun updateTitle(song:Track){
      val  text = "${song.title} - ${song.artiste}"
        binding.tvSongName.text = text
        glide.load(song.image_url).into(binding.ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){ result->
            when(result.status){
                Status.SUCCESS ->{
                    result.data?.let {
                        if (currentPlayingSong == null && it.isNotEmpty()){
                            currentPlayingSong = it[0]
                            updateTitle(it[0])
                        }
                    }
                }
                else -> Unit
            }
        }
        mainViewModel.currPlayingSong.observe(viewLifecycleOwner){
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            updateTitle(currentPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState = it
            binding.ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_baseline_pause_24
            else R.drawable.ic_baseline_play_arrow
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }
       songViewModel.curPlayerPosition.observe(viewLifecycleOwner){
            if (toUpdateSeekBar){
                binding.seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            binding.seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            binding.tvSongDuration.text = dateFormat.format(it)
        }

    }

    private fun setCurrentPlayerTimeToTextView(position: Long?) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvCurTime.text = dateFormat.format(position)
    }

}