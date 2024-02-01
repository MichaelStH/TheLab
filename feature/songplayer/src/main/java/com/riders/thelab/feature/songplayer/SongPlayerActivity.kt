package com.riders.thelab.feature.songplayer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.animation.addListener
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media.session.MediaButtonReceiver
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.data.local.model.music.SongModel
import com.riders.thelab.core.permissions.PermissionManager
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.songplayer.core.SongsManager
import com.riders.thelab.feature.songplayer.core.service.MusicMediaPlaybackService
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SongPlayerActivity : BaseComponentActivity() {

    private val viewModel: SongPlayerViewModel by viewModels()

    // Media Player
    private lateinit var mp: MediaPlayer

    // Handler to update UI timer, progress bar etc,.
    private var mHandler: Handler? = null
    private var songManager: SongsManager? = null
    // private val seekForwardTime = 5000 // 5000 milliseconds

    // private val seekBackwardTime = 5000 // 5000 milliseconds

    private var currentSongIndex = 0
    private val isShuffle = false
    private val isRepeat = false


    // Songs list
    private var songsList = ArrayList<SongModel>()

    private var isCardViewPlayerShown: Boolean = false
    private var isToggle: Boolean = false

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            val totalDuration = mp.duration.toLong()
            val currentDuration = mp.currentPosition.toLong()

            // Displaying Total Duration time
            //songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration))
            // Displaying time completed playing
            //songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration))

            // Updating progress bar
            //val progress = SongPlayerUtils.getProgressPercentage(currentDuration, totalDuration)
            //Log.d("Progress", ""+progress);
            //if (_viewBinding != null) binding.songProgressBar.progress = progress

            @Suppress("DEPRECATION")
            mHandler = Handler()
            // Running this thread after 100 milliseconds
            mHandler?.postDelayed(this, 100)
        }
    }

    private lateinit var controller: MediaControllerCompat
    private lateinit var mServiceMusic: MusicMediaPlaybackService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicMediaPlaybackService.LocalBinder
            mServiceMusic = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setContent {
                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            SongPlayerContent(viewModel.songList)
                        }
                    }
                }
            }
        }

        checkPermissions()
    }

    override fun onStart() {
        super.onStart()

        // Bind to LocalService
        Intent(this, MusicMediaPlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    @Deprecated("DEPRECATED - Use registerActivityForResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == 100) {
            currentSongIndex = data?.extras?.getInt("songIndex")!!
            // play selected song
            //  playSong(songsList[currentSongIndex])
        }
    }

    override fun onPause() {
        if (mp.isPlaying) {
            mp.pause()
        }
        super.onPause()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        Timber.d("onResume()")
        runCatching {
            registerReceiver(MediaButtonReceiver(), IntentFilter(Intent.ACTION_MEDIA_BUTTON))
        }
            .onFailure {
                Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
            }
            .onSuccess {
                Timber.d("runCatching - onSuccess() | app list fetched successfully")
            }
    }

    override fun onStop() {
        super.onStop()
        if (mp.isPlaying) {
            mp.stop()
            mp.reset()
            mp.release()
        }
        mHandler = null

        unbindService(connection)
        mBound = false
    }

    override fun backPressed() {
        if (viewModel.isViewToggle) {
            viewModel.toggleViewToggle(!isToggle)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy()")
    }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    /*private fun setListeners() {
        binding.tvSongPath.setOnClickListener(this)
        binding.btnArrowDown.setOnClickListener(this)
        binding.btnPlayPause.setOnClickListener(this)
        binding.btnPrevious.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.songProgressBar.setOnSeekBarChangeListener(this) // Important
        // mp.setOnCompletionListener(this) // Important
        binding.motionLayout.addTransitionListener(this)
    }*/

    @SuppressLint("NewApi")
    private fun checkPermissions() {
        PermissionManager
            .from(this@SongPlayerActivity)
            .request(
                if (LabCompatibilityManager.isTiramisu()) {
                    Permission.MediaLocationAndroid13
                } else {
                    Permission.Storage
                }
            )
            .rationale("Theses permissions are mandatory to fetch data")
            .checkPermission { granted: Boolean ->
                if (!granted) {
                    Timber.e("All permissions are not granted")
                } else {
                    Timber.i("All permissions are granted")


                    // Media Player
                    mp = MediaPlayer()
                    songManager = SongsManager()

                    viewModel.retrieveSongFiles(this@SongPlayerActivity)
                }
            }
    }


    /**
     * Function to play a song
     * @param item - index of song
     */
    /*@SuppressLint("InlinedApi")
    private fun playSong(item: SongModel) {
        Timber.d("playSong()")

        binding.songModel = item

        if (!isCardViewPlayerShown)
            changeViews()

        if (binding.cvSongPlayer.visibility != View.VISIBLE) {

            val params = binding.rvFileList.layoutParams
            val animator = ValueAnimator.ofInt(
                binding.rvFileList.height,
                binding.rvFileList.height - (binding.cvSongPlayer.height + (2 * binding.cvSongPlayer.marginTop))
            )
            animator.addUpdateListener { valueAnimator ->
                params.height = (valueAnimator.animatedValue as Int)
                binding.rvFileList.requestLayout()
            }
            animator.addListener(onEnd = { fadeViews() })
            animator.duration = 700
            animator.start()

            isCardViewPlayerShown = true
        }

        // Play song
        try {
            mp.reset()
            mp.setDataSource(item.path)
            mp.prepare()
            mp.start()

            // Displaying Song title via Notification
            LabNotificationManager.createNotificationChannel(
                this@SongPlayerActivity,
                getString(R.string.music_channel_name),
                getString(R.string.music_channel_description),
                NotificationManager.IMPORTANCE_HIGH,
                Constants.NOTIFICATION_MUSIC_CHANNEL_ID
            )

            val mediaSession = SongPlayerUtils.createMediaSession(this, mp)
            val mediaController = SongPlayerUtils.createMediaController(mediaSession)

            *//*LabNotificationManager.displayMusicNotification(
                this@SongPlayerActivity,
                mediaSession,
                mediaController,
                mServiceMusic,
                item
            )*//*

            //displaySessionNotification(item)

            // Changing Button Image to pause image
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

            // set Progress bar values
            binding.songProgressBar.progress = 0
            binding.songProgressBar.max = 100

            // Updating progress bar
            updateProgressBar()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }*/

    /*private fun changeViews() {
        CoroutineScope(coroutineContext).launch {
            makeViewVisible(binding.cvSongPlayer)
            makeViewVisible(binding.clSongPlayer)
            makeViewVisible(binding.tvSongName)
            makeViewVisible(binding.tvSongPath)
            makeViewVisible(binding.ivThumb)
            makeViewVisible(binding.songProgressBar)
            makeViewVisible(binding.btnPrevious)
            makeViewVisible(binding.btnPlayPause)
            makeViewVisible(binding.btnNext)
            makeViewVisible(binding.btnClose)
            makeViewVisible(binding.btnArrowDown)
        }
    }

    private fun fadeViews() {
        CoroutineScope(coroutineContext).launch {
            fadeView(binding.cvSongPlayer)
            fadeView(binding.clSongPlayer)
            fadeView(binding.tvSongName)
            fadeView(binding.tvSongPath)
            fadeView(binding.ivThumb)
            fadeView(binding.songProgressBar)
            fadeView(binding.btnPrevious)
            fadeView(binding.btnPlayPause)
            fadeView(binding.btnNext)
            fadeView(binding.btnClose)

        }
    }*/

    private suspend fun makeViewVisible(targetViewToVisible: View) {
        delay(200)
        targetViewToVisible.visibility = View.INVISIBLE
    }


    private suspend fun fadeView(targetViewToFade: View) {
        delay(250)

        val objectAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(targetViewToFade, View.ALPHA, 0f, 1f)

        objectAnimator.interpolator = LinearOutSlowInInterpolator()
        objectAnimator.duration = 800
        objectAnimator.addListener(
            onEnd = {
                Timber.d("onEnd()")
            }
        )

        objectAnimator.start()

        targetViewToFade.visibility = View.VISIBLE
    }

    /*private fun toggleAnimation(view: View) {
        if (!isToggle) {
            binding.motionLayout.transitionToEnd()
            // Disable card view click listener
            binding.cvSongPlayer.isClickable = false
            mAdapter.setClickable(false)
        } else {
            binding.motionLayout.transitionToStart()
            // Enable card view click listener
            binding.cvSongPlayer.isClickable = true
            mAdapter.setClickable(true)
        }
    }*/

    /**
     * Update timer on seekbar
     */
    private fun updateProgressBar() {
        mHandler?.postDelayed(mUpdateTimeTask, 100)
    }


    /*override fun onSongClick(view: View, item: SongModel) {
        Timber.d("onFileClick() - $item")

        playSong(item)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_arrow_down -> {
                // make iv clickable only if view is toggled
                if (isToggle)
                    toggleAnimation(view)
            }

            R.id.tv_song_path -> {
                if (!isToggle)
                    toggleAnimation(view)
            }

            R.id.btn_play_pause -> {
                // check for already playing
                if (mp.isPlaying) {
                    mp.pause()
                    // Changing button image to play button
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                } else {
                    // Resume song
                    mp.start()
                    // Changing button image to pause button
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }

            R.id.btn_previous -> {
                if (currentSongIndex > 0) {
                    playSong(songsList[currentSongIndex - 1])
                    currentSongIndex -= 1
                } else {
                    // play last song
                    playSong(songsList[songsList.size - 1])
                    currentSongIndex = songsList.size - 1
                }

            }

            R.id.btn_next -> {
                // check if next song is there or not
                if (currentSongIndex < (songsList.size - 1)) {
                    playSong(songsList[currentSongIndex + 1])
                    currentSongIndex += 1
                } else {
                    // play first song
                    playSong(songsList[0])
                    currentSongIndex = 0
                }
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(songsList[currentSongIndex])
        } else if (isShuffle) {
            // shuffle is on - play a random song
            val rand = Random()
            currentSongIndex = rand.nextInt(songsList.size - 1 - 0 + 1) + 0
            playSong(songsList[currentSongIndex])
        } else {
            // no repeat or shuffle ON - play next song
            currentSongIndex = if (currentSongIndex < songsList.size - 1) {
                playSong(songsList[currentSongIndex + 1])
                currentSongIndex + 1
            } else {
                // play first song
                playSong(songsList[0])
                0
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // ignored
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // remove message Handler from updating progress bar
        mHandler?.removeCallbacks(mUpdateTimeTask)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        mHandler?.removeCallbacks(mUpdateTimeTask)
        val totalDuration = mp.duration
        val currentPosition: Int =
            SongPlayerUtils.progressToTimer(seekBar!!.progress, totalDuration)

        // forward or backward to certain seconds
        mp.seekTo(currentPosition)

        // update timer progress again
        updateProgressBar()
    }

    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
        Timber.i("onTransitionStarted()")
    }

    override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
        // Ignored
    }

    override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
        Timber.e("onTransitionCompleted()")
        isToggle = !isToggle
    }

    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
        Timber.i("onTransitionTrigger()")
    }*/
}