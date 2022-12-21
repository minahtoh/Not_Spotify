package com.example.notspotify.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notspotify.R
import com.example.notspotify.adapters.SongAdapter
import com.example.notspotify.databinding.FragmentHomeBinding
import com.example.notspotify.other.Resource
import com.example.notspotify.other.Status
import com.example.notspotify.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
   lateinit var binding: FragmentHomeBinding
   lateinit var mainViewModel: MainViewModel
   @Inject
   lateinit var songAdapter: SongAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        songAdapter.setOnItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }

    }

    private fun subscribeToObservers(){
        binding.rvAllSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(activity)
            mainViewModel.mediaItems.observe(viewLifecycleOwner){ result->
                when(result.status){
                    Status.SUCCESS -> {
                        binding.allSongsProgressBar.isVisible = false
                        (adapter as SongAdapter).submitList(result.data)
                    }
                    Status.LOADING -> {
                        binding.allSongsProgressBar.isVisible = true
                    }
                    Status.ERROR -> Unit
                }

            }

        }
    }
}