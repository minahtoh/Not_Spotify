package com.example.notspotify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.notspotify.data.models.Track
import com.example.notspotify.databinding.ListItemBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide : RequestManager
): ListAdapter<Track,SongAdapter.OurViewHolder>(DiffCallback) {
   inner class OurViewHolder(private var binding:ListItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(song:Track){
            binding.apply {
                tvPrimary.text = song.title
                tvSecondary.text = song.artiste
                glide.load(song.image_url).into(ivItemImage)
            }
        }

    }
    companion object DiffCallback: DiffUtil.ItemCallback<Track>(){
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.media_Id == newItem.media_Id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OurViewHolder {
       return OurViewHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: OurViewHolder, position: Int) {
        val songPosition = getItem(position)
        holder.bind(songPosition)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(songPosition)
            }
        }
    }

    private var onItemClickListener:((Track)->Unit)? = null
    fun setOnItemClickListener(listener:(Track) -> Unit){
        onItemClickListener = listener
    }
}