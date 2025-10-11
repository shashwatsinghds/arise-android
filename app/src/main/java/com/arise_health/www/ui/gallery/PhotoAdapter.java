package com.arise_health.www.ui.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arise_health.www.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<File> photoFiles = new ArrayList<>();
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(File photoFile);
    }

    public PhotoAdapter(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void setPhotos(List<File> photos) {
        this.photoFiles = photos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        File photoFile = photoFiles.get(position);
        holder.bind(photoFile);
    }

    @Override
    public int getItemCount() {
        return photoFiles.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_photo);
        }

        public void bind(File photoFile) {
            // Load thumbnail
            Bitmap bitmap = decodeSampledBitmapFromFile(photoFile.getAbsolutePath(), 200, 200);
            imageView.setImageBitmap(bitmap);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(photoFile);
                }
            });
        }

        private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }
}

