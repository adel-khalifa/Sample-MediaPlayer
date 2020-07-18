package com.greycom.samplemediaplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.greycom.samplemediaplayer.Constants.CURRENT_INDEX
import com.greycom.samplemediaplayer.Constants.CURRENT_TIME
import com.greycom.samplemediaplayer.Constants.EXTRA_SONG_INDEX
import kotlinx.android.synthetic.main.activity_main.*


class MediaActivity : AppCompatActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener,
    MediaPlayer.OnCompletionListener {

    private val TAG = this.javaClass.simpleName
    private var currentTime: Int = 0
    var mediaPlayer = MediaPlayer()
    private lateinit var playList: ArrayList<Song>
    private var currentIndex = -1

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setMediaPlayer()
        setInitDataFromIntent()
        Log.e(TAG, "in onCreate:  playlist Size = " + playList.size)
        Log.e(TAG, "in onCreate:  current index = $currentIndex")


        if (savedInstanceState != null) {
            currentTime = savedInstanceState.getInt(CURRENT_TIME)
            Log.e(TAG, "in onCreate: (SavedInstanceState)  current Time = $currentTime")
            currentIndex = savedInstanceState.getInt(CURRENT_INDEX)
            Log.e(TAG, "in onCreate: (SavedInstanceState)  current index = $currentIndex")
        }
        setMediaState(playList[currentIndex])
        seekBarDurationThread()

        setUi()
        setListeners()


    }

    private fun seekBarDurationThread() = Thread(Runnable {
        while (mediaPlayer.isPlaying) {
            try {
                currentTime = mediaPlayer.currentPosition
                runOnUiThread{ seek_bar_progress.progress = currentTime }
                Thread.sleep(1000)
            } catch (e: Exception) {
                // TODO Error handling Exceptions
            }
        }
    }).start()

    private fun setMediaPlayer() {
        //   mediaPlayer = MediaPlayer()
        mediaPlayer.isLooping = true
    }

    override fun onProgressChanged(seekbar: SeekBar?, percent: Int, isByUser: Boolean) {
        when (seekbar) {
            seekBarVolume -> mediaPlayer.setVolume(percent / 100f, percent / 100f)
            seek_bar_progress -> if (isByUser) {
                currentTime = percent
                seekbar?.progress = percent
                mediaPlayer.seekTo(currentTime)
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        return
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        return
    }


    private fun setInitDataFromIntent() {
        playList = ArrayList<Song>()

        if (currentIndex == -1)
            currentIndex = intent.getIntExtra(EXTRA_SONG_INDEX, 0)
        Log.e(TAG, "in setInitDataFromIntent:  current index = $currentIndex")

        if (playList.size == 0) {
            playList = intent.getSerializableExtra(Constants.EXTRA_SONGS_LIST) as ArrayList<Song>
            Log.e(TAG, "in setInitDataFromIntent:  playlist Size = " + playList.size)
        }
    }

    private fun setUi() {
        seekBarVolume.progress = 50
        seek_bar_progress.max = mediaPlayer.duration
        seek_bar_progress.progress = currentTime
    }

    private fun setListeners() {
        seekBarVolume.setOnSeekBarChangeListener(this)
        seek_bar_progress.setOnSeekBarChangeListener(this)
        mediaPlayer.setOnCompletionListener(this)
        btnPlay.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        btn_previous.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_next -> playNext()
            btn_previous -> playPrevious()
            btnPlay -> {
                if (!mediaPlayer.isPlaying) pauseMedia()
                else resumeMedia()
            }
        }
    }

    private fun resumeMedia() {
        btnPlay.setBackgroundResource(R.drawable.ic_play_circle_24)
        mediaPlayer.pause()
    }

    private fun pauseMedia() {
        btnPlay.setBackgroundResource(R.drawable.ic_pause_circle_24)
        mediaPlayer.start()
    }

    override fun onCompletion(p0: MediaPlayer?) = playNext()

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_INDEX, currentIndex)
        outState.putInt(CURRENT_TIME, mediaPlayer.currentPosition)
    }

    fun getAlbumPicture(photoUri: String?): Bitmap? {
        val metaData = MediaMetadataRetriever()
        metaData.setDataSource(photoUri)
        val data = metaData.embeddedPicture
        return if (data != null) {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } else null
    }

    private fun setMediaState(song: Song) {
        if (currentIndex in 0 until playList.size) {
            Log.e(TAG, "in setMediaState():   current index = $currentIndex")

            handleViewDependingOnSongOrder()
            mediaPlayer.stop()
            mediaPlayer.reset()
            setMediaDataSourceAndView(song)
            startMedia()

        }
    }

    private fun setMediaDataSourceAndView(song: Song) {
        mediaPlayer.setDataSource(song.path)
        tv_title.text = song.title
        tv_artist.text = song.artist
        bg_image.setImageBitmap(getAlbumPicture(song.path))
    }

    private fun handleViewDependingOnSongOrder() {
        if (currentIndex < 1) btn_previous.visibility = View.INVISIBLE
        else btn_previous.visibility = View.VISIBLE

        if (currentIndex == playList.size - 1) btn_next.visibility = View.INVISIBLE
        else btn_next.visibility = View.VISIBLE
    }

    private fun startMedia() {
        mediaPlayer.prepare()
        mediaPlayer.seekTo(currentTime)
        mediaPlayer.start()
        btnPlay.setBackgroundResource(R.drawable.ic_pause_circle_24)
    }

    private fun playNext() {
        currentIndex += 1
        currentTime = 0
        setMediaState(playList[currentIndex])
    }

    private fun playPrevious() {
        currentIndex -= 1
        currentTime = 0
        setMediaState(playList[currentIndex])
    }

}

