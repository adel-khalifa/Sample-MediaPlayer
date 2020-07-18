package com.greycom.samplemediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.greycom.samplemediaplayer.Constants.EXTRA_SONGS_LIST
import com.greycom.samplemediaplayer.Constants.EXTRA_SONG_INDEX
import com.greycom.samplemediaplayer.Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity(), SongsAdapter.OnSongClickListener {
    private val TAG = this.javaClass.simpleName
    private lateinit var mediaList: ArrayList<Song>
    private lateinit var songsAdapter: SongsAdapter
    private var currentIndex = 0


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaList = ArrayList()
        songsAdapter = SongsAdapter()
        requestRequiredPermissions()
        setContentView(R.layout.activity_list)
        setRecycler()
        songsAdapter.setOnSongClickListener(this)
    }

    private fun setRecycler() {
        songs_rv.apply {
            layoutManager =
                LinearLayoutManager(this@ListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = songsAdapter

        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun loadMusic() {
        Log.e(TAG, "in loadMusic: Size = *****" + mediaList.size)

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                mediaList.add(
                    Song(
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                            .trim(),
                        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        photo = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST))
                    )
                )
            }
        }
        songsAdapter.asyncListDiffer.submitList(mediaList)
        Log.e(TAG, "in loadMusic: Size = " + mediaList.size)
    }

    override fun onSongClick(position: Int) {
        val intent = Intent(this, MediaActivity::class.java)
        intent.putExtra(EXTRA_SONG_INDEX, position)
        intent.putExtra(EXTRA_SONGS_LIST, mediaList)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaActivity().mediaPlayer.release()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE && grantResults.isNotEmpty()
            && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            loadMusic()

            Log.e(TAG, "in request permission: Size = " + mediaList.size)

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestRequiredPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            loadMusic()
            Log.e(TAG, "when reLunchApp: Size = " + mediaList.size)


        }
    }


}