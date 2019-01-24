package fr.wildcodeschool.mediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

import fr.WildCodeSchool.player.WildOnPlayerListener;
import fr.WildCodeSchool.player.WildPlayer;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
  // Audio player
  private WildPlayer mPlayer = null;
  // SeekBar
  private SeekBar mSeekBar = null;
  // SeekBar update delay
  private static final int SEEKBAR_DELAY = 1000;
  // Thread used to update the SeekBar position
  private final Handler mSeekBarHandler = new Handler();
  private Runnable mSeekBarThread;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialization of the wild audio player
    mPlayer = new WildPlayer(this);
    mPlayer.init(R.string.song, new WildOnPlayerListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        mSeekBar.setMax(mp.getDuration());
      }

      @Override
      public void onCompletion(MediaPlayer mp) {
        mSeekBarHandler.removeCallbacks(mSeekBarThread);
        mSeekBar.setProgress(0);
      }
    });

    // Initialization of the SeekBar
    mSeekBar = findViewById(R.id.seekBar);
    mSeekBar.setOnSeekBarChangeListener(this);

    // Thread used to update the SeekBar position according to the audio player
    mSeekBarThread = new Runnable() {
      @Override
      public void run() {
        // Widget should only be manipulated in UI thread
        mSeekBar.post(() -> mSeekBar.setProgress(mPlayer.getCurrentPosition()));
        // Launch a new request
        mSeekBarHandler.postDelayed(this, SEEKBAR_DELAY);
      }
    };
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mPlayer.release();
  }


  // --------------------------------------------------------------------------
  // SeekBar interface
  // --------------------------------------------------------------------------

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   * @param progress Current position on the SeekBar
   * @param fromUser Define if it is a user action or a programmatic seekTo
   */
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        mPlayer.seekTo(progress);
      }
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    Log.e("Activity", "onStartTrackingTouch");
    // Stop seekBarUpdate here
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    Log.e("Activity", "onStopTrackingTouch");
    // Restart seekBarUpdate here
    if (null != mPlayer && mPlayer.isPlaying()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }


  // --------------------------------------------------------------------------
  // Buttons onClick
  // --------------------------------------------------------------------------

  /**
   * On play button click
   * Launch the playback of the media
   */
  public void playMedia(View v) {
    if (null != mPlayer && mPlayer.play()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  /**
   * On pause button click
   * Pause the playback of the media
   */
  public void pauseMedia(View v) {
    if (null != mPlayer && mPlayer.pause()) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
    }
  }

  /**
   * On reset button click
   * Stop the playback of the media
   */
  public void resetMedia(View v) {
    if (null != mPlayer && mPlayer.reset()) {
      mSeekBar.setProgress(0);
    }
  }
}
