package com.example.imagecrop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button saveButton;
    ImageView imageview;

    private Uri mCropImageUri;
    ImageView btnAdd1;
    ImageView btnAdd2;
    Button btnSave;
    private Uri uriCropImg1 = null;
    private Uri uriCropImg2 = null;
    int p =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd1 = findViewById(R.id.btnAdd1);
        btnAdd2 = findViewById(R.id.btnAdd2);
        btnSave = findViewById(R.id.btnSave);
        btnAdd1.setImageResource(R.drawable.plus);
        btnAdd2.setImageResource(R.drawable.plus);
        btnAdd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p =1;
                CropImage.startPickImageActivity(MainActivity.this);
            }
        });
        btnAdd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p = 2;
                CropImage.startPickImageActivity(MainActivity.this);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergeImage();

            }
        });
    }

    private void mergeImage() {
        try {
            Bitmap bitmap1 = null;
            Bitmap bitmap2 = null;

            if (uriCropImg1 != null) {
                bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriCropImg1);
            }
            if (uriCropImg2 != null) {
                bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriCropImg2);
            }

            Bitmap bitmapResult = null;

            if (bitmap1 != null && bitmap2 != null) {
                bitmapResult = createMergeImage(bitmap1, bitmap2);
            } else if (bitmap1 != null) {
                bitmapResult = bitmap1;
            } else if (bitmap2 != null) {
                bitmapResult = bitmap2;
            }

            if (bitmapResult != null) {
                saveImage(bitmapResult);

            }

        } catch (IOException e) {
            Toast.makeText(this, "Ghép ảnh thất bại!", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap createMergeImage(Bitmap firstImage, Bitmap secondImage) {

        int w = Math.min(firstImage.getWidth(), secondImage.getWidth());
        int h1 = (int)(firstImage.getHeight() * (1.0 * w / firstImage.getWidth()));
        int h2 = (int)(secondImage.getHeight() * (1.0 * w / secondImage.getWidth()));

        Bitmap firstImageResized = Bitmap.createScaledBitmap(firstImage, w, h1, true);
        Bitmap secondImageResized = Bitmap.createScaledBitmap(secondImage, w, h2, true);

        Bitmap result = Bitmap.createBitmap(w, firstImageResized.getHeight() + secondImageResized.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImageResized, 0f, 0f, null);
        canvas.drawBitmap(secondImageResized, 0, firstImageResized.getHeight(), null);
        return result;
    }

    private void saveImage(Bitmap finalBitmap) {
        try {
            Date currentTime = Calendar.getInstance().getTime();
            String image_name = currentTime.toString().replace(":", "_").replace(" ", "_").replace("/", "_");
            String fname = "Merged-" + image_name + ".jpg";
            MediaStore.Images.Media.insertImage(getContentResolver(), finalBitmap, fname, "Merge image");
            Toast.makeText(this, "Đã lưu", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lưu ảnh xảy ra lỗi", Toast.LENGTH_LONG).show();
        }
    }

    public void onSaveImage(View v){
        Toast.makeText(this, "Đã lưu", Toast.LENGTH_LONG).show();
    }


    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                if(p == 1){
                    btnAdd1.setImageResource(0);
                    uriCropImg1 = result.getUri();
                    btnAdd1.setImageURI(uriCropImg1);
                } else{
                    btnAdd2.setImageResource(0);
                    uriCropImg2 = result.getUri();
                    btnAdd2.setImageURI(uriCropImg2);
                }
                Toast.makeText(this, "Crop thành công", Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Crop thất bại: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Yêu cầu truy cập", Toast.LENGTH_LONG).show();
        }
    }


    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}
