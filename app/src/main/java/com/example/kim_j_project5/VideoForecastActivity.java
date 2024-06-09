package com.example.kim_j_project5;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VideoForecastActivity extends AppCompatActivity {
    private String location;
    private VideoView videoView;
    private String videoFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent myIntent = getIntent();
        location = myIntent.getStringExtra("location");
        videoFileName = location + ".mp4";

        TextView videoTitle = findViewById(R.id.location_textView);
        videoTitle.setText(String.format("%s Forecast", location));
        videoView = findViewById(R.id.videoView);

        Button recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(v -> startVideoRecordingProcess());

        playVideo();
    }

    // play video if exists already
    private void playVideo() {
        File videoFile = new File(getFilesDir(), videoFileName);
        if (videoFile.exists()) {
            videoView.setVideoURI(Uri.fromFile(videoFile));
            videoView.start();
        } else {
            Toast.makeText(this, "No Video Available", Toast.LENGTH_SHORT).show();
        }
    }

    // let user record video
    private void recordVideo() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, 101);
            } catch (Exception e) {
                Log.i("HERE VIDEO", "record video e: " + e.getMessage());
            }
        } else {
            Log.i("HERE VIDEO", "no camera");
            Toast.makeText(this, "No Camera", Toast.LENGTH_SHORT).show();
        }
    }

    // handle video result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri videoURI = data.getData();
            if (videoURI != null) {
                videoView.setVideoURI(videoURI);
                videoView.start();
                saveVideo(videoURI);
            }
        }
    }

    // save video to local storage
    private void saveVideo(Uri videoURI) {
        try {
            File videoFile = new File(getFilesDir(), videoFileName);
            InputStream inputStream = getContentResolver().openInputStream(videoURI);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(videoFile);
                copyStream(inputStream, outputStream);
                inputStream.close();
                outputStream.close();

                // Set the video file to the VideoView and start playing
                videoView.setVideoURI(Uri.fromFile(videoFile));
                videoView.start();
            }
        } catch (IOException e) {
            Log.e("HERE VIDEO", "Error saving video: " + e.getMessage());
            Toast.makeText(this, "Error saving video", Toast.LENGTH_SHORT).show();
        }
    }

    // method to copy data from input to output stream
    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != 1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    // camera permissions
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 100);
        } else {
            recordVideo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordVideo();
            } else {
                Log.i("HERE VIDEO", "perms denied");
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startVideoRecordingProcess() {
        checkCameraPermission();
    }

}