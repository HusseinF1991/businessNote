package com.uruksys.businessnote_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class EnlargeImageToFullScreenActivity extends AppCompatActivity {

    ImageView imgEnlargedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarge_image_to_full_screen);


        imgEnlargedImage = findViewById(R.id.imgEnlargedImage);

        byte[] imageArray = getIntent().getByteArrayExtra("Image");

        Bitmap bitmap2 = BitmapFactory
                .decodeByteArray(imageArray, 0, imageArray.length);
        imgEnlargedImage.setImageBitmap(bitmap2);
    }
}
