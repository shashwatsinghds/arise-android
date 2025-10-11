package com.arise_health.www.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arise_health.www.databinding.FragmentGalleryBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private PhotoAdapter photoAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup RecyclerView with grid layout
        RecyclerView recyclerView = binding.recyclerViewGallery;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns

        // Setup adapter
        photoAdapter = new PhotoAdapter(this::openPhoto);
        recyclerView.setAdapter(photoAdapter);

        // Load photos
        loadPhotos();

        return root;
    }

    private void loadPhotos() {
        List<File> photoFiles = new ArrayList<>();

        // Get photos from app's private directory
        File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists()) {
            File[] files = picturesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            if (files != null && files.length > 0) {
                photoFiles.addAll(Arrays.asList(files));
            }
        }

        // Sort by last modified (newest first)
        Collections.sort(photoFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // Update UI
        if (photoFiles.isEmpty()) {
            binding.recyclerViewGallery.setVisibility(View.GONE);
            binding.textNoPhotos.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewGallery.setVisibility(View.VISIBLE);
            binding.textNoPhotos.setVisibility(View.GONE);
            photoAdapter.setPhotos(photoFiles);
        }
    }

    private void openPhoto(File photoFile) {
        try {
            // Open photo in system viewer
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.arise_health.www.fileprovider",
                    photoFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(photoURI, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload photos when fragment becomes visible
        loadPhotos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
