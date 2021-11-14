package com.example.lab4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;
    File file;
    Uri fileUri;
    int requestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        AudioManager audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVol = audioManager.
                getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol,
                AudioManager.FLAG_SHOW_UI);
    }

    public void play(View v) throws IOException {
        initializeMediaPlayer();
        if (mediaPlayer == null)
            return;
        mediaPlayer.start();
    }

    public void stop(View v) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void startRecord(View view) throws IOException {
        newRecordFile();
        initializeMediaRecorder();
        mediaRecorder.prepare();
        mediaRecorder.start();
        Toast.makeText(getApplicationContext(),"Nagrywanie trwa", Toast.LENGTH_SHORT).show();
    }

    public void stopRecord(View view) throws IOException {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Toast.makeText(getApplicationContext(),"Nagrywanie zatrzymane", Toast.LENGTH_SHORT).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (this.requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return;
            fileUri = data.getData();
            TextView fileTextView = this.findViewById(R.id.textView5);
            fileTextView.setText(getFileName(fileUri));
        }
    }

    public void fileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mpeg");
        startActivityForResult(intent, requestCode);
    }

    private void initializeMediaPlayer() {
        if (fileUri == null)
            return;
        mediaPlayer = MediaPlayer.create(this, fileUri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("WrongConstant")
    private void initializeMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(8000);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    private void newRecordFile() throws IOException {
        File rootDir = Environment.getExternalStorageDirectory();
        file = File.createTempFile("record", ".mp3", rootDir);
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}