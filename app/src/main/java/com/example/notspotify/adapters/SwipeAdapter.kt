package com.example.notspotify.adapters

import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notspotify.data.models.Track
import com.example.notspotify.databinding.SwipeItemBinding

class SwipeAdapter : ListAdapter<Track, SwipeAdapter.OurViewHolder>(Diffcallback) {
    inner class OurViewHolder(var binding: SwipeItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(song:Track){
            binding.apply {
               val text = "${song.title} - ${song.artiste}"
                tvPrimary.text = text

            }
        }
    }
    companion object Diffcallback: DiffUtil.ItemCallback<Track>(){
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.media_Id == newItem.media_Id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OurViewHolder {
        return OurViewHolder(SwipeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: OurViewHolder, position: Int) {
        val songPosition = getItem(position)
        holder.bind(songPosition)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(songPosition) }
        }
    }

    private var onItemClickListener:((Track)->Unit)? = null
    fun setOnItemClickListener(listener:(Track) -> Unit){
        onItemClickListener = listener
    }


}