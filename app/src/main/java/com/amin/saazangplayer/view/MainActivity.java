package com.amin.saazangplayer.view;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.amin.saazangplayer.App;
import com.amin.saazangplayer.R;
import com.amin.saazangplayer.constants.NotificationConstants;
import com.amin.saazangplayer.constants.PlayType;
import com.amin.saazangplayer.constants.SettingsConstant;
import com.amin.saazangplayer.interfaces.FavoriteRecently;
import com.amin.saazangplayer.interfaces.PlayListener;
import com.amin.saazangplayer.interfaces.SendFolderAlbumNameListener;
import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.modelview.FavoriteRecentlyViewModel;
import com.amin.saazangplayer.modelview.MusicViewModel;
import com.amin.saazangplayer.service.MusicPlayService;
import com.amin.saazangplayer.service.NotificationService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements PlayListener, AlbumFragment.SendAlbumNameListener,
        FolderFragment.SendFolderNameListener, View.OnClickListener {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final String FILE_PARH = "filePath";
    public static final String Broadcast_PLAY_NEW_MUSIC = "com.amin.chaaldir.PlayNewMusic";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Inject
    public MusicViewModel musicViewModel;
    @Inject
    public FavoriteRecentlyViewModel favRecenltViewModel;

    private MusicPlayService musicPlayService;
    private boolean isMusicServiceBound = false;

    private SendFolderAlbumNameListener sendFolderAlbumNameListener;
    private FavoriteRecently favoriteRecently;
    private List<AudioModel> selectedAudioModelList;
    private int musicIndex = -1;

    private PlayType playType = PlayType.Normal;

    private List<Integer> nextshuffleIndexList = new ArrayList<>();
    private List<Integer> previousShuffleIndexList = new ArrayList<>();
    private SecureRandom secureRandom = new SecureRandom();

    private ImageButton imageButtonPlayPause;
    private ImageButton imageButtonFavorite;
    private ImageButton imageButtonPlayType;
    private ImageButton imageButtonNext;
    private ImageButton imageButtonPrevious;
    private ImageButton imageButtonShowHidePlayer;
    private MusicFragment musicFragment;

    private LinearLayout linearPlayerPage;
    private SeekBar seekBarMusic;
    private MutableLiveData<Integer> musicTimeLeftStatus;
    private SeekBarStatusThread seekBarStatusThread;
    private TextView textViewTimeLeft;
    private ImageView imageViewMusicArt;
    private SeekBar seekBarVolume;
    private AudioModel audioModel;

    private static MainActivity mainActivity;
    private Intent notificationIntent;
    private EditText editTextSearch;

    private Resources.Theme appTheme;


    private boolean isSettingsChanged = false;

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayService.LocalBinder binder = (MusicPlayService.LocalBinder) iBinder;
            musicPlayService = binder.getService();
            isMusicServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isMusicServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        changeLanguage();
        changeAppTheme();

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainActivity = MainActivity.this;

        initialzeMaterials();
        navigationDrawerInitiate();
        selectFavoriteRecentlyPlayed();

        viewPager = findViewById(R.id.viewpager);
        setViewPager(viewPager);

        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        setTabsIcon(tabLayout);

        seekBarVolumeControl();
        seekBarMusicTracking();
        // hideKeyboard();

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            App.getApp().getsPlayerComponnent().inject(MainActivity.this);
        }

        appSettings();


        initialzePlayerPage();


    }


    public void initialzePlayerPage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    while (selectedAudioModelList == null) ;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AudioModel audioModel = null;
                            if (selectedAudioModelList != null && selectedAudioModelList.size() > 0) {
                                musicIndex = selectedAudioModelList.indexOf(favRecenltViewModel.getLatestMusic());
                                audioModel = favRecenltViewModel.getLatestMusic();
                                if (musicIndex == -1)
                                    audioModel = selectedAudioModelList.get(0);

                                if (audioModel != null) {
                                    seekBarMusic.setMax(Integer.parseInt(audioModel.getDuration()));
                                    seekBarMusic.setProgress(favRecenltViewModel.getLatestMusicPosition());
                                    textViewTimeLeft.setText(timeConverter(seekBarMusic.getProgress()));
                                    setPlayerPageContent(audioModel);
                                    scrollAndSetItemBackground(audioModel);
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


    private void initialzeMaterials() {
        imageButtonPlayPause = findViewById(R.id.imageButtonPlayPause);
        imageButtonFavorite = findViewById(R.id.imageButtonFavorite);
        imageButtonPlayType = findViewById(R.id.imageButtonPlayType);
        imageButtonNext = findViewById(R.id.imageButtonNext);
        imageButtonPrevious = findViewById(R.id.imageButtonPrevious);
        seekBarMusic = findViewById(R.id.seekbarMusic);
        imageButtonShowHidePlayer = findViewById(R.id.imageButtonShowHide);
        linearPlayerPage = findViewById(R.id.playerPage);
        textViewTimeLeft = findViewById(R.id.textviewLeftTime);
        imageViewMusicArt = findViewById(R.id.imageviewMusic);
        seekBarVolume = findViewById(R.id.seekbarVolume);
        editTextSearch = findViewById(R.id.editTextSearch);

        musicTimeLeftStatus = new MutableLiveData<>();

        imageButtonPlayPause.setOnClickListener(this);
        imageButtonFavorite.setOnClickListener(this);
        imageButtonPlayType.setOnClickListener(this);
        imageButtonNext.setOnClickListener(this);
        imageButtonPrevious.setOnClickListener(this);
        imageButtonShowHidePlayer.setOnClickListener(this);
        imageViewMusicArt.setOnClickListener(this);

        linearPlayerPage.setVisibility(View.GONE);

    }

    private void navigationDrawerInitiate() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


    }

    private class SeekBarStatusThread extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(1000);

                while (true) {

                    Thread.sleep(1000);
                    // if (musicPlayService != null && musicPlayService.mediaPlayer != null) {
                    int position = musicPlayService.mediaPlayer.getCurrentPosition();
                    musicTimeLeftStatus.postValue(position);
                    // }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMusicSeekBarStatus(final AudioModel audioModel) {

        seekBarMusic.setMax(Integer.parseInt(audioModel.getDuration()));

        musicTimeLeftStatus.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                seekBarMusic.setProgress(position);
                textViewTimeLeft.setText(timeConverter(position));
                if (musicPlayService != null && musicPlayService.mediaPlayer != null)
                    if (position >= musicPlayService.mediaPlayer.getDuration() / 2) {
                        if (!favRecenltViewModel.isExistInRecently(audioModel)) {
                            favRecenltViewModel.addRecentlyPlayed(audioModel);

                        }
                    }
            }
        });
        if (seekBarStatusThread == null) {
            seekBarStatusThread = new SeekBarStatusThread();
            seekBarStatusThread.start();
        } else {
            if (seekBarStatusThread.isAlive())
                seekBarStatusThread.interrupt();
            seekBarStatusThread = new SeekBarStatusThread();
            seekBarStatusThread.start();
        }

    }

    private void showHidePlayerWithAnimation() {
        int xTopLeft = 0;
        int yTopLeft = 0;

        //int xBottomRight = linearPlayerPage.getLeft() + linearPlayerPage.getRight();
        //int yBottomRight = linearPlayerPage.getTop() + linearPlayerPage.getBottom();
        int radius = Math.max(linearPlayerPage.getWidth(), linearPlayerPage.getHeight());

        Animator animator;
        if (linearPlayerPage.getVisibility() == View.VISIBLE) {
            animator = ViewAnimationUtils.createCircularReveal(linearPlayerPage, xTopLeft, yTopLeft, radius, 0);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    showHidePlayerPage();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            animator = ViewAnimationUtils.createCircularReveal(linearPlayerPage, xTopLeft, yTopLeft, 0, radius);
            animator.removeAllListeners();
            showHidePlayerPage();
        }

        animator.setDuration(400);
        animator.start();

    }

    public static String timeConverter(long millisecond) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Long.valueOf(millisecond)),
                TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(millisecond)) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Long.valueOf(millisecond)))
        );
    }

    private Intent broadCastIntent;

    public void playMusic(AudioModel audioModel) {
        if (!isMusicServiceBound) {
            Intent musicPlayIntent = new Intent(MainActivity.this, MusicPlayService.class);
            int temIndex = selectedAudioModelList.indexOf(favRecenltViewModel.getLatestMusic());

            if (temIndex > -1 && selectedAudioModelList.size() >= temIndex) {
                audioModel = selectedAudioModelList.get(temIndex);
                musicIndex = temIndex;
            }

            musicPlayIntent.putExtra(FILE_PARH, audioModel.getPath());
            startService(musicPlayIntent);
            bindService(musicPlayIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);

            AudioModel finalAudioModel = audioModel;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (musicPlayService != null && musicPlayService.mediaPlayer != null)
                                    if (musicPlayService.mediaPlayer.isPlaying()) {
                                        musicPlayService.mediaPlayer.seekTo(favRecenltViewModel.getLatestMusicPosition());
                                    }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }).start();

            /*if (notificationIntent == null) {
                notificationIntent = new Intent(MainActivity.this, NotificationService.class);
                notificationIntent.setAction(NotificationConstants.ACTION.STARTFOREGROUND_ACTION);
                startService(notificationIntent);
            }*/

        } else {
            if (broadCastIntent == null)
                broadCastIntent = new Intent(Broadcast_PLAY_NEW_MUSIC);
            broadCastIntent.putExtra(FILE_PARH, audioModel.getPath());
            sendBroadcast(broadCastIntent);
        }

       /* if (NotificationService.getInstance() != null) {
            NotificationService.getInstance().updateNotificationContents();
            //NotificationService.getInstance().startAndUpdateState();
        }*/

        notificationIntent = new Intent(MainActivity.this, NotificationService.class);
        notificationIntent.setAction(NotificationConstants.ACTION.STARTFOREGROUND_ACTION);
        startService(notificationIntent);


        imageButtonPlayPause.setImageResource(R.drawable.ic_baseline_stop_64);

        setPlayerPageContent(audioModel);

        updateMusicSeekBarStatus(audioModel);
        scrollAndSetItemBackground(audioModel);

        imageButtonFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_32);
        if (favRecenltViewModel.isExistInFavorites(audioModel)) {
            imageButtonFavorite.setImageResource(R.drawable.ic_baseline_favorite_32);
        }

        Log.d("ThreadsCount", String.valueOf(Thread.activeCount()));
    }

    protected void scrollAndSetItemBackground(AudioModel audioModel) {

        for (int i = 0; i < musicFragment.getRecyclerView().getChildCount(); i++) {
            View view = musicFragment.getRecyclerView().getChildAt(i);
            view.findViewById(R.id.linearLayoutMusicItem).setBackgroundColor(Color.WHITE);
            view.findViewById(R.id.linearLayoutMusicItem).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.item_background));
        }
        int musicIndexTemp = musicIndex;
        if (musicIndex < 0)
            musicIndexTemp = 0; // For the first time loading app or there is not last played index.

        musicFragment.getRecyclerView().smoothScrollBy(musicIndexTemp, 1, null, 100);
        musicFragment.getRecyclerView().smoothScrollToPosition(musicIndexTemp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            musicFragment.getRecyclerView().setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    LinearLayout linearLayout = view.findViewWithTag(audioModel.getName());
                    if (linearLayout != null && linearLayout.getTag().equals(audioModel.getName())) {
                        linearLayout.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.selectedItemBackgroundColor));
                    }
                }
            });
        }
        //for times list items number less for scrolling. ex: 1, 2, ....7
        LinearLayout linearLayout = musicFragment.getRecyclerView().findViewWithTag(audioModel.getName());
        if (linearLayout != null)
            linearLayout.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.selectedItemBackgroundColor));

    }


    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            saveCurrentState();

            musicViewModel.clear();
            if (isMusicServiceBound) {
                musicPlayService.stopSelf();
                unbindService(musicServiceConnection);
            }
            if (notificationIntent != null) {
                // notificationIntent.setAction(NotificationConstants.ACTION.STOPFOREGROUND_ACTION);
                //startService(notificationIntent);
                stopService(notificationIntent);

            }

            if (seekBarStatusThread != null && seekBarStatusThread.isAlive())
                seekBarStatusThread.interrupt();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }


    }


    @Override
    public void play(int selectedMusicIndex) {
        if (selectedMusicIndex < selectedAudioModelList.size()) {
            audioModel = selectedAudioModelList.get(selectedMusicIndex);
            musicIndex = selectedMusicIndex;

            if (favRecenltViewModel.getLatestMusic() != null)
                favRecenltViewModel.saveCurrentMusicIndexAndPosition(null, 0);

            playMusic(audioModel);
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof MusicFragment) {
            musicFragment = (MusicFragment) fragment;
            musicFragment.setPlayListener(this);
            editTextSearch.addTextChangedListener(musicFragment);
        }
        if (fragment instanceof FolderFragment) {
            ((FolderFragment) fragment).setSendFolderNameListener(this);
        }
        if (fragment instanceof AlbumFragment) {
            ((AlbumFragment) fragment).setSendAlbumNameListener(this);
        }
    }

    @Override
    public void sendAlbumName(String albumName) {
        sendFolderAlbumNameListener.receiveFolderAlbumName(albumName, true);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void sendFolderName(String folderName) {

        sendFolderAlbumNameListener.receiveFolderAlbumName(folderName, false);
        viewPager.setCurrentItem(0, true);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButtonPlayPause:
                playAndPause();
                break;
            case R.id.imageButtonFavorite:
                addRemoveFavorite();

                break;
            case R.id.imageButtonPlayType:
                playType();

                break;
            case R.id.imageButtonNext:
                nextMusic();

                break;
            case R.id.imageButtonPrevious:
                previousMusic();

                break;
            case R.id.imageButtonShowHide:
                showHidePlayerWithAnimation();

                break;
            case R.id.imageviewMusic:
                if (seekBarVolume.getVisibility() != View.VISIBLE)
                    seekBarVolume.setVisibility(View.VISIBLE);
                else
                    seekBarVolume.setVisibility(View.GONE);

                break;

        }
        hideKeyboard();

    }

    private void addRemoveFavorite() {
        if (audioModel != null) {
            favRecenltViewModel.addRemoveFavorite(audioModel);

            imageButtonFavorite.setImageResource(R.drawable.ic_baseline_favorite_32);
            if (!favRecenltViewModel.isExistInFavorites(audioModel)) {
                imageButtonFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_32);
            }
        }


    }


    private void showHidePlayerPage() {
        if (linearPlayerPage.getVisibility() == View.GONE) {
            linearPlayerPage.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            imageButtonShowHidePlayer.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        } else {
            linearPlayerPage.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            imageButtonShowHidePlayer.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);

        }
    }

    public void previousMusic() {

        if (playType == PlayType.Normal) {
            if (musicIndex > 0)
                musicIndex--;

        } else if (playType == PlayType.This) {
            musicIndex = musicIndex;

        } else if (playType == PlayType.Shuffle) {
            musicIndex = shuffleIndexPrevious();

        } else if (playType == PlayType.Loop)
            if (musicIndex == 0)
                musicIndex = selectedAudioModelList.size() - 1;
            else
                musicIndex--;

        if (selectedAudioModelList != null && selectedAudioModelList.size() > 0) {
            audioModel = selectedAudioModelList.get(musicIndex);
            playMusic(audioModel);
        }


    }

    public void nextMusic() {

        if (playType == PlayType.Normal) {
            if (musicIndex < selectedAudioModelList.size() - 1)
                musicIndex++;

        } else if (playType == PlayType.This) {
            if (musicIndex == -1) musicIndex = 0;

        } else if (playType == PlayType.Shuffle) {
            musicIndex = shuffleIndexNext();

        } else if (playType == PlayType.Loop)
            if (musicIndex >= selectedAudioModelList.size() - 1)
                musicIndex = 0;
            else
                musicIndex++;


        if (selectedAudioModelList != null && selectedAudioModelList.size() > 0) {
            audioModel = selectedAudioModelList.get(musicIndex);
            playMusic(audioModel);
        }

    }

    public int shuffleIndexPrevious() {
        int randomIndex;
        if (nextshuffleIndexList.size() > 0) {
            randomIndex = nextshuffleIndexList.remove(nextshuffleIndexList.size() - 1);

        } else {
            if (musicIndex > -1)
                previousShuffleIndexList.add(musicIndex);
            randomIndex = secureRandom.nextInt(selectedAudioModelList.size());


        }
        return randomIndex;
    }

    public int shuffleIndexNext() {
        int randomIndex;
        if (previousShuffleIndexList.size() > 0) {
            randomIndex = previousShuffleIndexList.remove(previousShuffleIndexList.size() - 1);

        } else {
            if (musicIndex > -1)
                nextshuffleIndexList.add(musicIndex);
            randomIndex = secureRandom.nextInt(selectedAudioModelList.size());

        }
        return randomIndex;
    }

    public boolean playAndPause() {
        boolean isPlaying = true;
        if (musicPlayService == null || musicPlayService.mediaPlayer == null) {
            nextMusic();
        } else {
            if (musicPlayService.mediaPlayer.isPlaying()) {
                musicPlayService.mediaPlayer.pause();
                imageButtonPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_64);

                if (seekBarStatusThread != null && seekBarStatusThread.isAlive()) {
                    seekBarStatusThread.interrupt();
                    seekBarStatusThread = null;
                }

                isPlaying = false;


            } else {
                musicPlayService.mediaPlayer.start();
                imageButtonPlayPause.setImageResource(R.drawable.ic_baseline_stop_64);

                seekBarStatusThread = new SeekBarStatusThread();
                seekBarStatusThread.start();
            }


        }
        if (NotificationService.getInstance() != null) {
            if (isPlaying)
                NotificationService.getInstance().getBigViews().setImageViewResource(R.id.imageButtonPlayNotification, R.drawable.ic_baseline_stop_64);
            else
                NotificationService.getInstance().getBigViews().setImageViewResource(R.id.imageButtonPlayNotification, R.drawable.ic_baseline_play_arrow_64);

            NotificationService.getInstance().startAndUpdateState();
        }
        return isPlaying;
    }

    private void playType() {
        nextshuffleIndexList.clear();
        previousShuffleIndexList.clear();
        switch (playType) {
            case Normal:
                playType = PlayType.Shuffle;
                imageButtonPlayType.setImageResource(R.drawable.ic_baseline_shuffle_32);
                break;
            case Shuffle:
                playType = PlayType.This;
                imageButtonPlayType.setImageResource(R.drawable.ic_baseline_transform_32);
                break;
            case This:
                playType = PlayType.Loop;
                imageButtonPlayType.setImageResource(R.drawable.ic_baseline_loop_24);
                break;
            case Loop:
                playType = PlayType.Normal;
                imageButtonPlayType.setImageResource(R.drawable.ic_baseline_trending_flat_32);
                break;
        }
    }

    private void setPlayerPageContent(AudioModel audioModel) {
        TextView textViewMusic = findViewById(R.id.textviewMusicPLayerPage);
        TextView textViewArtist = findViewById(R.id.textviewArtistPLayerPage);
        TextView textViewAlbum = findViewById(R.id.textviewAlbumPLayerPage);
        TextView textViewTotalDuration = findViewById(R.id.textviewTotalDuration);

        textViewTotalDuration.setText(timeConverter(Long.parseLong(audioModel.getDuration())));
        Bitmap bitmap = musicViewModel.getAlbumArt(audioModel.getPath());

        textViewMusic.setText(getString(R.string.music_name) + ": " + audioModel.getName());
        textViewArtist.setText(getString(R.string.artist) + ": " + audioModel.getArtist());
        textViewAlbum.setText(getString(R.string.album_name) + ": " + audioModel.getAlbum());
        imageViewMusicArt.setImageBitmap(bitmap);
        linearPlayerPage.setBackgroundColor(getSampleColor(60));

    }

    private void setTabsIcon(TabLayout tabLayout) {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_queue_music_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_folder_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_album_24);

    }

    private void setViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.POSITION_NONE);
        viewPagerAdapter.addFragment(new MusicFragment(), getString(R.string.all_music));
        viewPagerAdapter.addFragment(new FolderFragment(), getString(R.string.folders));
        viewPagerAdapter.addFragment(new AlbumFragment(), getString(R.string.albums));

        viewPager.setAdapter(viewPagerAdapter);
    }


    public static boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    private static void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(MainActivity.getInstance().getString(R.string.dialog_title));
        alertBuilder.setMessage(msg + MainActivity.getInstance().getString(R.string.dialog_title));
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{permission},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    restartApp();


                } else {
                    //showMessage("مجوز رد شد!");

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    protected void restartApp() {
        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        this.finish();
    }


    public int getSampleColor(int alpha) {
        Random random = new Random();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewMusicArt.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        int pixel = bitmapDrawable.getBitmap().getPixel(random.nextInt(bitmap.getWidth()), random.nextInt(bitmap.getHeight()));
        int redVal = Color.red(pixel);
        int greenVal = Color.green(pixel);
        int blueVal = Color.blue(pixel);

        return Color.argb(alpha, redVal, greenVal, blueVal);
    }

    private void seekBarVolumeControl() {
        final AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int index, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (seekBarVolume.getProgress() > 0)
                    seekBarVolume.setProgress(seekBarVolume.getProgress() - 1);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (seekBarVolume.getProgress() < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
                    seekBarVolume.setProgress(seekBarVolume.getProgress() + 1);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void seekBarMusicTracking() {
        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int seekBarProgress = seekBarMusic.getProgress();
                if (musicPlayService != null && musicPlayService.mediaPlayer != null) {
                    musicPlayService.mediaPlayer.seekTo(seekBarProgress);
                    if (!musicPlayService.mediaPlayer.isPlaying())
                        playAndPause(); // play
                    else {
                        playAndPause(); //stop
                        playAndPause(); //play again
                    }

                } else {
                    playAndPause(); // Run for the first time
                }


            }
        });

    }

    private void selectFavoriteRecentlyPlayed() {
        NavigationView navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.recent_music:
                        favoriteRecently.receive(SettingsConstant.RECENTLY_PLAYED);
                        viewPager.setCurrentItem(0);
                        MainActivity.this.initialzePlayerPage();
                        break;
                    case R.id.favorite_music:
                        favoriteRecently.receive(SettingsConstant.FAVORITE_MUSIC);
                        viewPager.setCurrentItem(0);
                        MainActivity.this.initialzePlayerPage();
                        break;
                    case R.id.app_settings:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;

                }
                DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    protected void saveCurrentState() {
        favRecenltViewModel.saveCurrentList(selectedAudioModelList);
        if (musicIndex > -1 && selectedAudioModelList.size() > musicIndex)
            favRecenltViewModel.saveCurrentMusicIndexAndPosition(selectedAudioModelList.get(musicIndex), seekBarMusic.getProgress());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void appSettings() {
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.app_settings_preferences, false);
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                .registerOnSharedPreferenceChangeListener(settingsChangeListener);


    }


    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case SettingsConstant.APP_LANGUAGE:
                    //changeLanguage(sharedPreferences, key);
                    break;
                case SettingsConstant.APP_THEME:
                    //changeAppTheme();
                    break;
            }
            MainActivity.this.isSettingsChanged = true;

        }
    };

    private void changeAppTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String keyValue = sharedPreferences.getString(SettingsConstant.APP_THEME, "Theme 1");
        switch (keyValue) {

            case "Theme 1":
                appTheme.applyStyle(R.style.Theme1, true);
                //setTheme(R.style.Theme1);
                break;
            case "Theme 2":
                appTheme.applyStyle(R.style.Theme2, true);
                // setTheme(R.style.Theme2);
                break;
            case "Theme 3":
                appTheme.applyStyle(R.style.Theme3, true);
                // setTheme(R.style.Theme3);


                break;
            case "Theme 4":
                appTheme.applyStyle(R.style.Theme4, true);
                // setTheme(R.style.Theme4);
                break;
            case "Theme 5":
                appTheme.applyStyle(R.style.Theme5, true);
                // setTheme(R.style.Theme5);
                break;

        }


        // onCreate(savedInstanceState);

    }


    private void changeLanguage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String keyValue = sharedPreferences.getString(SettingsConstant.APP_LANGUAGE, "en");

        Locale locale;
        //Log.e("Lan",session.getLanguage());
        locale = new Locale(keyValue);
        Configuration config = new Configuration(getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());


    }


    @Override
    public Resources.Theme getTheme() {
        if (appTheme == null) {
            appTheme = super.getTheme();
            appTheme.applyStyle(R.style.Theme1, true);
        }
        return appTheme;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (linearPlayerPage.getVisibility() == View.VISIBLE)
            showHidePlayerWithAnimation();
        else
            this.moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if activity backed from settings replay
      /*  if (getIntent() != null && getIntent().getStringExtra(SettingsActivity.BACKED_FROM_SETTINGS) != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //try {
                    //TimeUnit.SECONDS.sleep(5);
                    while (MainActivity.getInstance().getSelectedAudioModelList() == null) {
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playAndPause();
                        }
                    });

                    // } catch (InterruptedException e) {
                    //e.printStackTrace();
                    // }

                }
            }).start();
            setIntent(null);
        }

*/
    }

    public void setSendFolderAlbumNameListener(SendFolderAlbumNameListener sendFolderAlbumNameListener) {
        this.sendFolderAlbumNameListener = sendFolderAlbumNameListener;
    }

    public List<AudioModel> getSelectedAudioModelList() {
        return selectedAudioModelList;
    }

    public void setSelectedAudioModelList(List<AudioModel> selectedAudioModelList) {
        this.selectedAudioModelList = selectedAudioModelList;
    }

    public int getMusicIndex() {
        return musicIndex;
    }

    public void setMusicIndex(int musicIndex) {
        this.musicIndex = musicIndex;
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

    public void setFavoriteRecently(FavoriteRecently favoriteRecently) {
        this.favoriteRecently = favoriteRecently;
    }

    public EditText getEditTextSearch() {
        return editTextSearch;
    }

    public List<Integer> getNextshuffleIndexList() {
        return nextshuffleIndexList;
    }

    public List<Integer> getPreviousShuffleIndexList() {
        return previousShuffleIndexList;
    }

    public boolean isSettingsChanged() {
        return isSettingsChanged;
    }


}