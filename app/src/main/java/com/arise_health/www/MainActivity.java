package com.arise_health.www;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.arise_health.www.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    
    // Camera related constants and variables
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 1;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    
    // Camera related methods
    private void openCamera() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, open camera
            dispatchTakePictureIntent();
        }
    }
    
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Create the File where the photo should be saved
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.d("Camera", "Image file created: " + photoFile.getAbsolutePath());
            Toast.makeText(this, "File created, opening camera...", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Log.e("CameraError", "Error creating image file", ex);
            Toast.makeText(this, "Error creating image file: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // If the file was created successfully, proceed with the camera intent
        if (photoFile != null) {
            try {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.arise_health.www.fileprovider",
                        photoFile);
                Log.d("Camera", "FileProvider URI created: " + photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                
                try {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } catch (Exception e) {
                    Log.e("CameraError", "Error starting camera", e);
                    Toast.makeText(this, "Unable to open camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("CameraError", "FileProvider error", e);
                Toast.makeText(this, "FileProvider error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        // Create the image file
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        
        // Save the file path for later use
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                dispatchTakePictureIntent();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Check if the file actually exists
                if (currentPhotoPath != null) {
                    File photoFile = new File(currentPhotoPath);
                    if (photoFile.exists()) {
                        long fileSize = photoFile.length();
                        Log.d("Camera", "Photo saved to: " + currentPhotoPath);
                        Log.d("Camera", "File size: " + fileSize + " bytes");
                        
                        // Add photo to gallery so it shows up in Photos app
                        addPhotoToGallery();
                    } else {
                        Toast.makeText(this, "ERROR: Photo file doesn't exist!", Toast.LENGTH_LONG).show();
                        Log.e("Camera", "Photo file doesn't exist at: " + currentPhotoPath);
                    }
                } else {
                    Toast.makeText(this, "ERROR: currentPhotoPath is null!", Toast.LENGTH_LONG).show();
                    Log.e("Camera", "currentPhotoPath is null");
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the camera
                Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unknown result code: " + resultCode, Toast.LENGTH_SHORT).show();
                Log.d("Camera", "Result code: " + resultCode);
            }
        }
    }
    
    private void addPhotoToGallery() {
        File photoFile = new File(currentPhotoPath);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above - use MediaStore API
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ARISEHealth");
            
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            if (uri != null) {
                try {
                    java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    java.io.FileInputStream inputStream = new java.io.FileInputStream(photoFile);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    
                    outputStream.close();
                    inputStream.close();
                    
                    Toast.makeText(this, "Photo saved to Gallery!", Toast.LENGTH_SHORT).show();
                    Log.d("Camera", "Photo added to gallery: " + uri.toString());
                } catch (Exception e) {
                    Log.e("Camera", "Error copying to gallery", e);
                    Toast.makeText(this, "Error saving to gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Android 9 and below - use MediaScanner
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photoFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            
            Toast.makeText(this, "Photo saved to Gallery!", Toast.LENGTH_SHORT).show();
            Log.d("Camera", "Photo scanned for gallery");
        }
    }
}