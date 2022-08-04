package com.example.musicplayerapp;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NewSongDialogFragment.OnSaveNewSong{
    final String DIALOG_FRAGMENT_TAG = "dialog_fragment";
    final String IMAGE_FRAGMENT_TAG = "image_dialog_fragment";
    final String PREFS_NAME = "MyPrefsFile";

    public static ArrayList<Song> songList;
    NewSongDialogFragment fragment;
    BigPictureSongDialogFragment imageFragment;
    SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton new_song_btn = findViewById(R.id.add_new_song_button);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewSongs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songList = new ArrayList<>();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            songList.add(new Song("Shape Of My Heart", "Sting", "https://upload.wikimedia.org/wikipedia/en/1/19/Fields-of-gold-sting.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
            songList.add(new Song("Lucid Dreams", "Juice World", "https://i.ytimg.com/vi/mzB1VGEGcSU/hqdefault.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3a"));
            songList.add(new Song("אסתדר לבד", "אייל גולן", "https://i.ytimg.com/vi/0HJEARRKNxM/hqdefault.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"));
            songList.add(new Song("Kryptonite", "3 Doors Down", "https://i.ytimg.com/vi/xPU8OAjjS4k/hq720.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"));
            songList.add(new Song("Lose Yourself", "Eminem", "https://i.ytimg.com/vi/Iw5BiCxOR-c/hq720.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
            songList.add(new Song("The Kids Aren't Alright", "The Offspring", "https://i.ytimg.com/vi/7iNbnineUCI/hq720.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
            songList.add(new Song("The Immortal", "Evanescence", "https://i.ytimg.com/vi/RxrTVf2vkLs/hq720.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"));
            settings.edit().putBoolean("my_first_time", false).commit();
        }else {
            try {
                FileInputStream fis = openFileInput("Songs");
                ObjectInputStream ois = new ObjectInputStream(fis);
                songList = (ArrayList<Song>) ois.readObject();
                ois.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        songAdapter = new SongAdapter(songList, this);
        songAdapter.setListener(new SongAdapter.MySongListener() {
            @Override
            public void onSongClicked(int position, View view) {
                Song song = songList.get(position);
                imageFragment = BigPictureSongDialogFragment.newInstance(song.getSongName(),song.getSongWriter(),
                        song.getLinkPicture());
                imageFragment.show(getSupportFragmentManager(), IMAGE_FRAGMENT_TAG);
                Intent intent=new Intent(MainActivity.this,MusicService.class);
                intent.putExtra("command","new_instance");
                intent.putExtra("position",position);
                startService(intent);
            }
        });
        ItemTouchHelper.Callback callback = new MyItemTouchHelper(songAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(songAdapter);

        new_song_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = NewSongDialogFragment.newInstance(songList.size());
                fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        });
    }
    @Override
    public void onSave(Song song) {
        songList.add(song);
        fragment.dismiss();
        songAdapter.notifyItemInserted(songList.size()-1);
        Toast.makeText(this, getResources().getString(R.string.song_save), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            FileOutputStream fos = openFileOutput("Songs",MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(songList);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}