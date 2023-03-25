package com.uruksys.businessnote_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShowItemInfoActivity extends AppCompatActivity {

    Uri itemImageUri, itemAttachmentUri;
    String itemName;
    byte[] itemImage, itemAttachment;
    String itemSellPrice, itemBoughtPrice;
    String itemId;
    EditText etxtItemName, etxtItemSellPrice, etxtItemBoughtPrice, etxtItemNotes;
    AutoCompleteTextView etxtItemSupplier;
    ImageView picbItemImage, picbItemAttachment;
    Button btnItemImg_camera, btnItemImg_gallery, btnItemAttachment_camera, btnItemAttachment_gallery, btnUpdateItem, btnCancelActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_info);

        itemId = String.valueOf(getIntent().getIntExtra("itemId", 0));

        etxtItemName = findViewById(R.id.etxtItemName);
        etxtItemSellPrice = findViewById(R.id.etxtItemSellPrice);
        etxtItemBoughtPrice = findViewById(R.id.etxtItemBoughtPrice);
        etxtItemSupplier = findViewById(R.id.etxtItemSupplier);
        etxtItemNotes = findViewById(R.id.etxtItemNotes);

        picbItemImage = findViewById(R.id.picbItemImage);
        picbItemAttachment = findViewById(R.id.picbItemAttachment);

        btnItemImg_camera = findViewById(R.id.btnItemImg_camera);
        btnItemImg_gallery = findViewById(R.id.btnItemImg_gallery);
        btnItemAttachment_camera = findViewById(R.id.btnItemAttachment_camera);
        btnItemAttachment_gallery = findViewById(R.id.btnItemAttachment_gallery);
        btnUpdateItem = findViewById(R.id.btnUpdateItem);
        btnCancelActivity = findViewById(R.id.btnCancelActivity);

        MySqliteDB mySqliteDB = new MySqliteDB(this, AgentsDataBasesActivity.selectedDbName);
        Cursor c = mySqliteDB.GetItemInfo_items(itemId);


        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    itemName = c.getString(c.getColumnIndex("itemName"));
                    etxtItemName.setText(itemName);

                    itemBoughtPrice = AES.decrypt(c.getString(c.getColumnIndex("boughtPrice")), AgentsDataBasesActivity.selectedDbSecretKey);
                    etxtItemBoughtPrice.setText(itemBoughtPrice);

                    itemSellPrice = AES.decrypt(c.getString(c.getColumnIndex("sellPrice")), AgentsDataBasesActivity.selectedDbSecretKey);
                    etxtItemSellPrice.setText(itemSellPrice);

                    etxtItemSupplier.setText(c.getString(c.getColumnIndex("supplier")));
                    etxtItemNotes.setText(c.getString(c.getColumnIndex("notes")));

                    itemImage = c.getBlob(c.getColumnIndex("itemImage"));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(itemImage, 0, itemImage.length);
                    picbItemImage.setImageBitmap(bitmap);


                    if (c.getBlob(c.getColumnIndex("attachment")) != null) {

                        itemAttachment = c.getBlob(c.getColumnIndex("attachment"));
                        Bitmap bitmap2 = BitmapFactory
                                .decodeByteArray(itemAttachment, 0, itemAttachment.length);
                        picbItemAttachment.setImageBitmap(bitmap2);
                    }

                } while (c.moveToNext());
            }
            c.close();
        }


        // Find the toolbar view inside the activity layout
        Toolbar dirActivityToolbar = (Toolbar) findViewById(R.id.toolbar_ShowItemInfoActivity);
        dirActivityToolbar.setTitle(itemName);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(dirActivityToolbar);


        //open camera to set item image
        btnItemImg_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ShowItemInfoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowItemInfoActivity.this, new String[]{Manifest.permission.CAMERA}, AddNewItemActivity.MY_CAMERA_PERMISSION_CODE_IMAGE);
                } else {
                    //
                    //
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    itemImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, itemImageUri);
                    startActivityForResult(intent, AddNewItemActivity.CAMERA_REQUEST_IMAGE);
                    //
                    //
                    //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, AddNewItemActivity.CAMERA_REQUEST_IMAGE);
                }
            }
        });

        //open camera to set image for item attachment
        btnItemAttachment_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ShowItemInfoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowItemInfoActivity.this, new String[]{Manifest.permission.CAMERA}, AddNewItemActivity.MY_CAMERA_PERMISSION_CODE_ATTACHMENT);
                } else {
                    //
                    //
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    itemAttachmentUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, itemAttachmentUri);
                    startActivityForResult(intent, AddNewItemActivity.CAMERA_REQUEST_ATTACHMENT);
                    //
                    //

                    //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, CAMERA_REQUEST_ATTACHMENT);
                }
            }
        });

        //open gallery to set item image
        btnItemAttachment_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ShowItemInfoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowItemInfoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AddNewItemActivity.MY_GALLERY_PERMISSION_CODE_ATTACHMENT);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, AddNewItemActivity.GALLERY_REQUEST_ATTACHMENT);
                }
            }
        });

        //open gallery to set item attachment image
        btnItemImg_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ShowItemInfoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowItemInfoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AddNewItemActivity.MY_GALLERY_PERMISSION_CODE_IMAGE);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, AddNewItemActivity.GALLERY_REQUEST_IMAGE);
                }
            }
        });


        btnCancelActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        OnUpdatingItem();
        AddAutoCompleteSourceToSupplier();
        ShowImageInFullScreen();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AddNewItemActivity.MY_CAMERA_PERMISSION_CODE_IMAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                //
                //
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                itemImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, itemImageUri);
                startActivityForResult(intent, AddNewItemActivity.CAMERA_REQUEST_IMAGE);
                //
                //
                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA_REQUEST_IMAGE);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == AddNewItemActivity.MY_CAMERA_PERMISSION_CODE_ATTACHMENT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                //
                //
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                itemAttachmentUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, itemAttachmentUri);
                startActivityForResult(intent, AddNewItemActivity.CAMERA_REQUEST_ATTACHMENT);
                //
                //

                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA_REQUEST_ATTACHMENT);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == AddNewItemActivity.MY_GALLERY_PERMISSION_CODE_IMAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, AddNewItemActivity.GALLERY_REQUEST_IMAGE);
            } else {
                Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == AddNewItemActivity.MY_GALLERY_PERMISSION_CODE_ATTACHMENT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, AddNewItemActivity.GALLERY_REQUEST_ATTACHMENT);
            } else {
                Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void AddAutoCompleteSourceToSupplier() {

        MySqliteDB mySqliteDB = new MySqliteDB(this, AgentsDataBasesActivity.selectedDbName);
        Cursor c = mySqliteDB.GetSuppliers_items();


        List<String> suppliersList = new ArrayList<>();
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    suppliersList.add(c.getString(c.getColumnIndex("supplier")));

                } while (c.moveToNext());
            }
            c.close();
        }

        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suppliersList);
        etxtItemSupplier.setAdapter(adapter);

    }


    private void OnUpdatingItem() {
        btnUpdateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etxtItemName.getText().toString().trim().equals("") ||
                        etxtItemBoughtPrice.getText().toString().equals("") ||
                        etxtItemSellPrice.getText().toString().equals("") ||
                        itemImage == null) {

                    Toast.makeText(ShowItemInfoActivity.this, "ادخل معلومات المنتج رجاءا", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPref = ShowItemInfoActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                    String dbSecretKey = sharedPref.getString("BusinessNoteAdminDbSecretKeySharedPrefereces", "");

                    String itemName = etxtItemName.getText().toString().trim();
                    String boughtPrice = AES.encrypt(etxtItemBoughtPrice.getText().toString(), dbSecretKey);
                    String sellPrice = AES.encrypt(etxtItemSellPrice.getText().toString(), dbSecretKey);
                    String supplier = etxtItemSupplier.getText().toString().trim();
                    String notes = etxtItemNotes.getText().toString().trim();

                    MySqliteDB mySqliteDB = new MySqliteDB(ShowItemInfoActivity.this, AgentsDataBasesActivity.selectedDbName);
                    mySqliteDB.UpdateItemInfo_items(itemId, itemName, boughtPrice, sellPrice, supplier, notes, itemImage, itemAttachment);

                    finish();
                }
            }
        });
    }


    private void ShowImageInFullScreen() {
        picbItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (itemImage != null) {

                    Intent intent = new Intent(ShowItemInfoActivity.this, EnlargeImageToFullScreenActivity.class);
                    intent.putExtra("Image", itemImage);
                    startActivity(intent);
                }
            }
        });


        picbItemAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemAttachment != null) {

                    Intent intent = new Intent(ShowItemInfoActivity.this, EnlargeImageToFullScreenActivity.class);
                    intent.putExtra("Image", itemAttachment);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddNewItemActivity.CAMERA_REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), itemImageUri);

                //re-rotate image if its rotated
                thumbnail = CheckImageRotation(UriUtils.getPathFromUri(getApplicationContext() , itemImageUri) , thumbnail);

                Bitmap resizedBitmap = resizeBitmap(thumbnail);
                picbItemImage.setImageBitmap(resizedBitmap);
                itemImage = AgentsDataBasesActivity.getBytesFromBitmap(resizedBitmap);

                //remove image from storage
                getContentResolver().delete(itemImageUri , null , null);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /*Bitmap photo = (Bitmap) data.getExtras().get("data");
            picbItemImage.setImageBitmap(photo);
            itemImage = MainActivity.getBytesFromBitmap(photo);*/
        } else if (requestCode == AddNewItemActivity.CAMERA_REQUEST_ATTACHMENT && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), itemAttachmentUri);

                //re-rotate image if its rotated
                thumbnail = CheckImageRotation(UriUtils.getPathFromUri(getApplicationContext() , itemAttachmentUri) , thumbnail);

                Bitmap resizedBitmap = resizeBitmap(thumbnail);
                picbItemAttachment.setImageBitmap(resizedBitmap);
                itemAttachment = AgentsDataBasesActivity.getBytesFromBitmap(resizedBitmap);


                //remove image from storage
                getContentResolver().delete(itemAttachmentUri , null , null);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            //picbItemAttachment.setImageBitmap(photo);
            //itemAttachment = MainActivity.getBytesFromBitmap(photo);
        } else if (requestCode == AddNewItemActivity.GALLERY_REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                if (selectedImage.getHeight() < 731 && selectedImage.getWidth() < 551) {

                    picbItemImage.setImageBitmap(selectedImage);
                    itemImage = AgentsDataBasesActivity.getBytesFromBitmap(selectedImage);
                } else {

                    Toast.makeText(this, "حجم الصورة كبير , رجاءا اختر صورة اصغر حجما", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ShowItemInfoActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == AddNewItemActivity.GALLERY_REQUEST_ATTACHMENT && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                if (selectedImage.getHeight() < 731 && selectedImage.getWidth() < 551) {

                    picbItemAttachment.setImageBitmap(selectedImage);
                    itemAttachment = AgentsDataBasesActivity.getBytesFromBitmap(selectedImage);
                } else {

                    Toast.makeText(this, "حجم الصورة كبير , رجاءا اختر صورة اصغر حجما", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ShowItemInfoActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }


    private Bitmap resizeBitmap(Bitmap thumbnail) {

        int width = thumbnail.getWidth();
        Log.i("imageSize", "itemAttachment Old width................" + width + "");
        int height = thumbnail.getHeight();
        Log.i("imageSize", "itemAttachment Old height................" + height + "");

        Matrix matrix = new Matrix();
        int newWidth = 496;
        int newHeight = 661;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        width = resizedBitmap.getWidth();
        Log.i("imageSize", "itemAttachment new width................" + width + "");
        height = resizedBitmap.getHeight();
        Log.i("imageSize", "itemAttachment new height................" + height + "");

        return resizedBitmap;
    }



    private Bitmap CheckImageRotation(String photoPath, Bitmap bitmap){
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
