package com.uruksys.businessnote_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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


public class AddNewItemActivity extends AppCompatActivity {

    Button btnNewItemImg_camera, btnNewItemImg_gallery, btnNewItemAttachment_camera, btnNewItemAttachment_gallery, btnAddNewItem;
    ImageView picbItemImage, picbNewItemAttachment;
    EditText etxtNewItemBoughtPrice, etxtNewItemSellPrice, etxtNewItemName, etxtNewItemNotes;
    AutoCompleteTextView etxtNewItemSupplier;

    Uri itemImageUri, itemAttachmentUri;
    byte[] itemImageStr, itemAttachmentStr;
    String parentDirId;

    public static final int MY_CAMERA_PERMISSION_CODE_IMAGE = 100;
    public static final int MY_CAMERA_PERMISSION_CODE_ATTACHMENT = 101;
    public static final int MY_GALLERY_PERMISSION_CODE_IMAGE = 102;
    public static final int MY_GALLERY_PERMISSION_CODE_ATTACHMENT = 103;
    public static final int WRITE_EXTERNAL_DATA_PERMISSION_CODE_EXPORT = 105;
    public static final int WRITE_EXTERNAL_DATA_PERMISSION_CODE_IMPORT = 106;
    public static final int PICKUP_FILE_REQUEST = 1892;
    public static final int CAMERA_REQUEST_IMAGE = 1888;
    public static final int CAMERA_REQUEST_ATTACHMENT = 1889;
    public static final int GALLERY_REQUEST_IMAGE = 1890;
    public static final int GALLERY_REQUEST_ATTACHMENT = 1891;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);


        btnNewItemImg_camera = (Button) findViewById(R.id.btnNewItemImg_camera);
        btnNewItemImg_gallery = (Button) findViewById(R.id.btnNewItemImg_gallery);
        btnNewItemAttachment_camera = (Button) findViewById(R.id.btnNewItemAttachment_camera);
        btnNewItemAttachment_gallery = (Button) findViewById(R.id.btnNewItemAttachment_gallery);
        btnAddNewItem = (Button) findViewById(R.id.btnAddNewItem);

        picbItemImage = (ImageView) findViewById(R.id.picbItemImage);
        picbNewItemAttachment = (ImageView) findViewById(R.id.picbNewItemAttachment);

        etxtNewItemName = findViewById(R.id.etxtNewItemName);
        etxtNewItemSellPrice = findViewById(R.id.etxtNewItemSellPrice);
        etxtNewItemBoughtPrice = findViewById(R.id.etxtNewItemBoughtPrice);
        etxtNewItemSupplier = findViewById(R.id.etxtNewItemSupplier);
        etxtNewItemNotes = findViewById(R.id.etxtNewItemNotes);

        parentDirId = getIntent().getStringExtra("parentDirId");

        //open camera to set item image
        btnNewItemImg_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(AddNewItemActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewItemActivity.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE_IMAGE);
                } else {
                    //
                    //
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    itemImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, itemImageUri);
                    startActivityForResult(intent, CAMERA_REQUEST_IMAGE);
                    //
                    //
                    //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, CAMERA_REQUEST_IMAGE);
                }
            }
        });

        //open camera to set image for item attachment
        btnNewItemAttachment_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(AddNewItemActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewItemActivity.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE_ATTACHMENT);
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
                    startActivityForResult(intent, CAMERA_REQUEST_ATTACHMENT);
                    //
                    //

                    //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, CAMERA_REQUEST_ATTACHMENT);
                }
            }
        });

        //open gallery to set item image
        btnNewItemAttachment_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AddNewItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewItemActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_PERMISSION_CODE_ATTACHMENT);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, GALLERY_REQUEST_ATTACHMENT);
                }
            }
        });

        //open gallery to set item attachment image
        btnNewItemImg_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AddNewItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddNewItemActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_PERMISSION_CODE_IMAGE);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, GALLERY_REQUEST_IMAGE);
                }
            }
        });


        AddNewItem();
        AddAutoCompleteSourceToSupplier();
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
        etxtNewItemSupplier.setAdapter(adapter);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE_IMAGE) {
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
                startActivityForResult(intent, CAMERA_REQUEST_IMAGE);
                //
                //
                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA_REQUEST_IMAGE);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_CAMERA_PERMISSION_CODE_ATTACHMENT) {
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
                startActivityForResult(intent, CAMERA_REQUEST_ATTACHMENT);
                //
                //

                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA_REQUEST_ATTACHMENT);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_GALLERY_PERMISSION_CODE_IMAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST_IMAGE);
            } else {
                Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_GALLERY_PERMISSION_CODE_ATTACHMENT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST_ATTACHMENT);
            } else {
                Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {

            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), itemImageUri);

                //re-rotate image if its rotated
                thumbnail = CheckImageRotation(UriUtils.getPathFromUri(getApplicationContext() , itemAttachmentUri) , thumbnail);

                Bitmap resizedBitmap = resizeBitmap(thumbnail);
                picbItemImage.setImageBitmap(resizedBitmap);
                itemImageStr = AgentsDataBasesActivity.getBytesFromBitmap(resizedBitmap);


                //remove image from storage
                getContentResolver().delete(itemImageUri , null , null);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /*Bitmap photo = (Bitmap) data.getExtras().get("data");
            picbItemImage.setImageBitmap(photo);
            itemImageStr = AgentsDataBasesActivity.getBytesFromBitmap(photo);*/
        } else if (requestCode == CAMERA_REQUEST_ATTACHMENT && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), itemAttachmentUri);

                //re-rotate image if its rotated
                thumbnail = CheckImageRotation(UriUtils.getPathFromUri(getApplicationContext() , itemAttachmentUri) , thumbnail);

                Bitmap resizedBitmap = resizeBitmap(thumbnail);

                picbNewItemAttachment.setImageBitmap(resizedBitmap);
                itemAttachmentStr = AgentsDataBasesActivity.getBytesFromBitmap(resizedBitmap);

                //remove image from storage
                getContentResolver().delete(itemAttachmentUri , null , null);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            //picbNewItemAttachment.setImageBitmap(photo);
            //itemAttachmentStr = AgentsDataBasesActivity.getBytesFromBitmap(photo);
        } else if (requestCode == GALLERY_REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                if (selectedImage.getHeight() < 731 && selectedImage.getWidth() < 551) {

                    picbItemImage.setImageBitmap(selectedImage);
                    itemImageStr = AgentsDataBasesActivity.getBytesFromBitmap(selectedImage);
                } else {

                    Toast.makeText(AddNewItemActivity.this, "حجم الصورة كبير , رجاءا اختر صورة اصغر حجما", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AddNewItemActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == GALLERY_REQUEST_ATTACHMENT && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                if (selectedImage.getHeight() < 731 && selectedImage.getWidth() < 551) {

                    picbNewItemAttachment.setImageBitmap(selectedImage);
                    itemAttachmentStr = AgentsDataBasesActivity.getBytesFromBitmap(selectedImage);
                } else {

                    Toast.makeText(AddNewItemActivity.this, "حجم الصورة كبير , رجاءا اختر صورة اصغر حجما", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AddNewItemActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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


    private void AddNewItem() {
        btnAddNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etxtNewItemName.getText().toString().trim().equals("") ||
                        etxtNewItemBoughtPrice.getText().toString().equals("") ||
                        etxtNewItemSellPrice.getText().toString().equals("") ||
                        itemImageStr == null) {

                    Toast.makeText(AddNewItemActivity.this, "ادخل معلومات المنتج رجاءا", Toast.LENGTH_SHORT).show();
                } else {

                    String itemName = etxtNewItemName.getText().toString().trim();
                    String boughtPrice = AES.encrypt(etxtNewItemBoughtPrice.getText().toString(), AgentsDataBasesActivity.selectedDbSecretKey);
                    String sellPrice = AES.encrypt(etxtNewItemSellPrice.getText().toString(), AgentsDataBasesActivity.selectedDbSecretKey);
                    String supplier = etxtNewItemSupplier.getText().toString().trim();
                    String notes = etxtNewItemNotes.getText().toString().trim();

                    MySqliteDB mySqliteDB = new MySqliteDB(AddNewItemActivity.this, AgentsDataBasesActivity.selectedDbName);
                    mySqliteDB.InsertNewItem_items(itemName, boughtPrice, sellPrice, supplier, notes, itemImageStr, parentDirId, itemAttachmentStr);

                    finish();
                }
            }
        });
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
