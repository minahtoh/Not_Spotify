package com.example.notspotify.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.notspotify.R
import com.example.notspotify.data.models.Track
import com.example.notspotify.databinding.FragmentSongBinding
import com.example.notspotify.other.Status
import com.example.notspotify.ui.toSong
import com.example.notspotify.ui.viewmodels.MainViewModel
import com.example.notspotify.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [SongFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class SongFragment : Fragment() {
   private lateinit var binding: FragmentSongBinding
   @Inject
   lateinit var glide:RequestManager
   private lateinit var mainViewModel: MainViewModel
   private val songViewModel: SongViewModel by viewModels()
    private var currentPlayingSong : Track? = null


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
                            updateTitle(currentPlayingSong!!)
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
    }

}