package com.example.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION = 10;

    ListView myListView;
    ArrayList<String> items;
    Cursor cursor;
    ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        MusicList(0);
    }

    //許可の確認
    private void checkPermission() {
        //外部ストレージへのアクセス許可がされてるか
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
    }

    //許可を取る
    private void requestLocationPermission() {
        if(!shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast toast = Toast.makeText(this,
                    "許可がないためアプリが実行できません",Toast.LENGTH_SHORT);
            toast.show();
        }
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
    }

    //結果の受け取り
    @Override
    public  void onRequestPermissionsResult(
            int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_PERMISSION){
            //使用を拒否された時
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません",Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    public void AllMusicClick(View view){
        MusicList(0);
    }
    public void AlbumClick(View view){
        MusicList(1);
    }
    public void ArtistClick(View view){
        MusicList(2);
    }

    public void MusicList(int L){
        //リスト
        myListView = findViewById(R.id.myListView);
        items = new ArrayList<>();
        //読み取り
        contentResolver = getContentResolver();
        cursor = null;
        int i = 0;
        //データ収納スペース
        int data;
        final String[] texts;
        final long[] thisId;
        //例外を受け取る
        try{
            cursor = contentResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if(L == 0){
                //AllMusic
                data = cursor.getCount();
                texts = new String[data];
                thisId = new long[data];
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        //曲タイトル
                        items.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        texts[i] = (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        thisId[i] = (cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        i++;
                    } while (cursor.moveToNext());
                    cursor.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,        //第一引数:content
                            R.layout.list_item,  //第二引数:行のレイアウト指定
                            items                //第三引数:データ
                    );
                    // ListViewに表示
                    myListView.setAdapter(adapter);
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent();
                            intent.putExtra("TITLE", texts);
                            intent.putExtra("Music", thisId);
                            intent.putExtra("index", position);
                            intent.setClass(MainActivity.this, MusicPlay.class);
                            startActivity(intent);
                        }
                    });
                }
            }else if(L == 1) {
                //ALBUM
                data = cursor.getCount();
                texts = new String[data];
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        //アルバム名
                        if( !items.contains(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)))) {
                            items.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            texts[i] = (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            i++;
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,        //第一引数:content
                            R.layout.list_item,  //第二引数:行のレイアウト指定
                            items                //第三引数:データ
                    );
                    // ListViewに表示
                    myListView.setAdapter(adapter);
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String check = texts[position];
                            ListChange(check);
                        }
                    });
                }
            }else{
                //ARTIST
                data = cursor.getCount();
                texts = new String[data];
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        //アーティスト名
                        if( !items.contains(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))) {
                            items.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            texts[i] = (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            i++;
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,        //第一引数:content
                            R.layout.list_item,  //第二引数:行のレイアウト指定
                            items                //第三引数:データ
                    );
                    // ListViewに表示
                    myListView.setAdapter(adapter);
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String check = texts[position];
                            ListChange(check);
                        }
                    });
                }
            }
        }catch(Exception e){
            e.printStackTrace();

            Toast toast = Toast.makeText(this,
                    "例外が発生、Permissionを許可していますか？", Toast.LENGTH_SHORT);
            toast.show();

            //MainActivityに戻す
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public void ListChange(String check){
        ArrayList<String> List = new ArrayList<>();
        cursor = contentResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        int i = 0;
        int data= cursor.getCount();
        String[] NewTexts = new String[data];
        long[] NewThisId = new long[data];
        if (cursor != null && cursor.moveToFirst()) {

            do {
                if (check.equals(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM ))) ||
                        check.equals(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))) {

                    List.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    NewTexts[i] = (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    NewThisId[i] = (cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    i++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> AlbumAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item,
                List
        );
        myListView.setAdapter(AlbumAdapter);
        int finalI = i;
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                String[] T = new String[finalI];
                long[] Id = new long[finalI];
                for(int z=0; z<finalI;z++){
                    T[z]=NewTexts[z];
                    Id[z]=NewThisId[z];
                }
                intent.putExtra("TITLE", T);
                intent.putExtra("Music", Id);
                intent.putExtra("index", position);
                intent.setClass(MainActivity.this, MusicPlay.class);
                startActivity(intent);
            }
        });
    }
}