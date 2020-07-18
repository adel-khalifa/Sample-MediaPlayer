package com.greycom.samplemediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song_element.view.*

class SongsAdapter() : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {
    private lateinit var listener: OnSongClickListener

    inner class SongsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class DiffImpl : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

    }

    private val diffCallBack = DiffImpl()

    // List of Data place holder / the object which can access from outside the adapter to add/update data
    val asyncListDiffer = AsyncListDiffer(this, diffCallBack)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.song_element, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {

        asyncListDiffer.currentList.let {
            val song = it[position]
            holder.itemView.apply {
               // Glide.with(context).load(song.photo).into(song_image)
                song_image.setImageBitmap(MediaActivity().getAlbumPicture(song.path))
                song_title.text = song.title
                song_artist.text = song.artist

                setOnClickListener {
                    listener.onSongClick(position)
                }

            }
        }

    }

    interface OnSongClickListener {
        fun onSongClick(position: Int)
    }

    fun setOnSongClickListener(onSongClickListener: OnSongClickListener) {
        listener = onSongClickListener
    }
}
