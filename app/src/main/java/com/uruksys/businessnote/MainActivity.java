package com.uruksys.businessnote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    Button btnEnterRootDir, btnExportDatabase, btnImportDatabase, btnDeleteAllData, btnLogOut;


    final int permissionForImportingDbCode = 1991 ;
    final int permissionForExportingDbCode = 1992;
    String backUpFilePath_ForImporting = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnEnterRootDir = findViewById(R.id.btnEnterRootDir);
        btnExportDatabase = findViewById(R.id.btnExportDatabase);
        btnImportDatabase = findViewById(R.id.btnImportDatabase);
        btnDeleteAllData = findViewById(R.id.btnDeleteAllData);
        btnLogOut = findViewById(R.id.btnLogOut);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_MainActivity);
        toolbar.setTitle("businessNote");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);


        BtnClickedToEnterDirs();
        BtnClickedToImportDb();
        BtnClickedToExportDb();
        FormatDataBase();
        LogOut();
    }


    private void BtnClickedToEnterDirs() {

        btnEnterRootDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");
                if (dbSecretKey.equals("")) {

                    LayoutInflater inflater = getLayoutInflater();
                    View view1 = inflater.inflate(R.layout.db_secret_key_dialog, null);
                    final EditText etxtSecretKey = view1.findViewById(R.id.etxtSecretKey);
                    Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
                    Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setView(view1);
                    final AlertDialog alertDialog = builder.create();

                    btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (etxtSecretKey.getText().toString().trim().length() < 6) {
                                Toast.makeText(MainActivity.this, "رمز الشفرة يجب ان لا يقل عن 6 مراتب", Toast.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("BusinessNoteDbSecretKeySharedPrefereces", etxtSecretKey.getText().toString().trim());
                                editor.commit();
                                alertDialog.dismiss();
                            }
                        }
                    });
                    btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                } else {

                    Intent intent = new Intent(MainActivity.this, DirActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    private void BtnClickedToImportDb() {

        btnImportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("application/octet-stream");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, AddNewItemActivity.PICKUP_FILE_REQUEST);
            }
        });
    }


    private void LogOut() {

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("BusinessNoteKeepSignedInSharedPrefereces", false);
                editor.commit();

                MainActivity.this.finish();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }


    //when user forgets the secret key of his database he promotes the app to format db and change cipher key
    private void FormatDataBase() {
        btnDeleteAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("لايمكنك استعادة البيانات عند فقدان رمز الشفرة, هل ترغب بحذف البيانات وتوليد نسخة جديدة")
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MySqliteDB mySqliteDB = new MySqliteDB(MainActivity.this);
                                mySqliteDB.RecreateDb();


                                final SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);

                                final SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("BusinessNoteDbSecretKeySharedPrefereces", "");
                                editor.commit();

                                LayoutInflater inflater = getLayoutInflater();
                                View view1 = inflater.inflate(R.layout.db_secret_key_dialog, null);
                                final EditText etxtSecretKey = view1.findViewById(R.id.etxtSecretKey);
                                Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
                                Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                        .setView(view1);
                                final AlertDialog alertDialog = builder.create();

                                btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (etxtSecretKey.getText().toString().trim().length() < 6) {
                                            Toast.makeText(MainActivity.this, "رمز الشفرة يجب ان لا يقل عن 6 مراتب", Toast.LENGTH_SHORT).show();
                                        } else {
                                            editor.putString("BusinessNoteDbSecretKeySharedPrefereces", etxtSecretKey.getText().toString().trim());
                                            editor.commit();
                                            alertDialog.dismiss();

                                            Toast.makeText(MainActivity.this, "تم توليد قاعدة بيانات جديدة", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertDialog.dismiss();
                                    }
                                });


                                alertDialog.show();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }


    private void BtnClickedToExportDb() {
        btnExportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = getLayoutInflater();
                View view1 = inflater.inflate(R.layout.export_db_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setView(view1);

                Button btnSaveNSendDbCopy = view1.findViewById(R.id.btnSaveNSendDbCopy);
                Button btnSaveCopy = view1.findViewById(R.id.btnSaveCopy);

                final AlertDialog alertDialog = builder.create();

                btnSaveCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                                (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                            ActivityCompat.requestPermissions(MainActivity.this
                                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}
                                    , permissionForExportingDbCode);
                        } else {

                            SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            MySqliteDB mySqliteDB = new MySqliteDB(MainActivity.this);
                            mySqliteDB.UpdateDbCipherKey_dbCipherKey(dbSecretKey, "user");

                            exportDB();

                            mySqliteDB.UpdateDbCipherKey_dbCipherKey("uRuKrUbIsHcIpHeR", "user");

                        }
                        alertDialog.dismiss();
                    }
                });


                btnSaveNSendDbCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                        (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                            ActivityCompat.requestPermissions(MainActivity.this
                                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}
                                    , permissionForExportingDbCode);
                        } else {

                            SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            MySqliteDB mySqliteDB = new MySqliteDB(MainActivity.this);
                            mySqliteDB.UpdateDbCipherKey_dbCipherKey(dbSecretKey, "user");

                            exportDB();

                            mySqliteDB.UpdateDbCipherKey_dbCipherKey("uRuKrUbIsHcIpHeR", "user");


                            //share db copy by any social media
                            Intent myIntent = new Intent(Intent.ACTION_SEND);
                            myIntent.setType("application/octet-stream");
                            //File dbFile = new File(Environment.getExternalStorageDirectory() + File.separator + "BusinessNoteDBs" + File.separator +
                            //        "BusinessNoteDb" + ".db");
                            Log.d("exportDb" ,  "4: "+getExternalFilesDir(null).getPath());
                            Log.d("exportDb" , "5: "+Environment.getExternalStorageDirectory().getPath());
                            File dbFile = new File(getExternalFilesDir(null) + File.separator + "BusinessNoteDBs"
                                    ,"BusinessNoteDb" + ".db");
                            Log.d("exportDb" , "6: "+ dbFile.getPath());
                            //File dbFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                            //      "BusinessNoteDb" + ".db");


                            Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.uruksys.businessnote.provider", dbFile);
                            myIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            //myIntent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("db"));
                            //myIntent.setPackage("com.whatsapp");  //send only by whatsapp
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(myIntent, "Share using"));
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionForExportingDbCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "تم السماح بحفظ بيانات في ذاكرة الهاتف", Toast.LENGTH_LONG).show();

                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                MySqliteDB mySqliteDB = new MySqliteDB(MainActivity.this);
                mySqliteDB.UpdateDbCipherKey_dbCipherKey(dbSecretKey, "user");

                exportDB();

                mySqliteDB.UpdateDbCipherKey_dbCipherKey("uRuKrUbIsHcIpHeR", "user");

            } else {
                Toast.makeText(this, "write to external storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == permissionForImportingDbCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importDB(backUpFilePath_ForImporting);

            } else {
                Toast.makeText(this, "write to external storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    //convert byte[] to bitmap
    public static Bitmap decodeBase64Profile(String input) {
        Bitmap bitmap = null;
        if (input != null) {
            byte[] decodedByte = Base64.decode(input, 0);
            bitmap = BitmapFactory
                    .decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return bitmap;
    }


    //convert bitmap to byte[] to set it in db in blob variable
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }
        return null;
    }


    private void exportDB() {
        try {
            File dbFile = new File(this.getDatabasePath("BusinessNoteDb").getAbsolutePath());

            //save twice , step 1:
            //save in application folders com.uruksys.businessnote
            FileInputStream fis = new FileInputStream(dbFile);
            File folder = new File(getExternalFilesDir(null) + File.separator + "BusinessNoteDBs");
            Log.d("exportDb", "1: "+folder.getPath());
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();

                if (!success) {
                    success = folder.mkdirs();
                }

                Log.d("exportDb", "2: dir making process is : " + success);
            }
            if (success) {
                String outFileName = folder.getPath() + File.separator + "BusinessNoteDb" + ".db";
                Log.d("exportDb", "3: "+outFileName);


                // Open the empty db as the output stream
                OutputStream output = new FileOutputStream(outFileName);

                // Transfer bytes from the input file to the output file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                // Close the streams
                output.flush();
                output.close();
                fis.close();

                //step 2:
                //save in external storage
                FileInputStream fis2 = new FileInputStream(dbFile);
                File folder2 = new File(Environment.getExternalStorageDirectory() + File.separator + "BusinessNoteDBs");
                Log.d("exportDb", "1: "+folder2.getPath());
                boolean success2 = true;
                if (!folder2.exists()) {
                    success2 = folder2.mkdir();

                    if (!success2) {
                        success2 = folder2.mkdirs();
                    }

                    Log.d("exportDb", "2: dir making process is : " + success2);
                }
                if (success2) {
                    String outFileName2 = folder2.getPath() + File.separator + "BusinessNoteDb" + ".db";
                    //String outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                    //        "BusinessNoteDb" + ".db";
                    Log.d("exportDb", "3: " + outFileName2);


                    // Open the empty db as the output stream
                    OutputStream output2 = new FileOutputStream(outFileName2);

                    // Transfer bytes from the input file to the output file
                    byte[] buffer2 = new byte[1024];
                    int length2;
                    while ((length2 = fis2.read(buffer2)) > 0) {
                        output2.write(buffer2, 0, length2);
                    }
                    // Close the streams
                    output2.flush();
                    output2.close();
                    fis2.close();
                }

                Toast.makeText(this, "تم عمل نسخة احتياطية وخزنها في مجلد BusinessNoteDBs", Toast.LENGTH_SHORT).show();

            }
        } catch (IOException e) {
            Log.e("dbBackup:", e.getMessage());
        }
    }


    private void importDB(String filePath) {
        /*
         * Try to open the file for "read" access using the
         * returned URI. If the file isn't found, write to the
         * error log and return.
         */
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data1 = Environment.getDataDirectory();

            if (sd.canWrite()) {
                //String  currentDBPath= "//data//" + "PackageName" + "//databases//" + "DatabaseName";
                String currentDBPath = this.getDatabasePath("BusinessNoteDb").getAbsolutePath();
                File currentDB = new File(currentDBPath);
                Log.d("ImportDB", "importDB 1: "+currentDB.toString());

                String backupDBPath = filePath;
                File backupDB = new File(backupDBPath);
                Log.d("ImportDB", "importDB 2: "+backupDB.toString());

                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                final SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                String dbCipherKey = null;
                String fromWhere = null;
                MySqliteDB mySqliteDB = new MySqliteDB(this);
                Cursor c = mySqliteDB.GetDbCipherKey_dbCipherKey();
                if (c.getCount() > 0) {
                    if (c.moveToFirst()) {
                        do {
                            dbCipherKey = c.getString(c.getColumnIndex("cipherKey"));
                            fromWhere = c.getString(c.getColumnIndex("fromWhere"));
                        } while (c.moveToNext());
                    }
                    c.close();
                }


                if (fromWhere.equals("user")) {

                    LayoutInflater inflater = getLayoutInflater();
                    View view1 = inflater.inflate(R.layout.db_secret_key_dialog, null);
                    final EditText etxtSecretKey = view1.findViewById(R.id.etxtSecretKey);
                    Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
                    Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setView(view1);
                    final AlertDialog alertDialog = builder.create();

                    btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (etxtSecretKey.getText().toString().trim().length() < 6) {
                                Toast.makeText(MainActivity.this, "رمز الشفرة يجب ان لا يقل عن 6 مراتب", Toast.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("BusinessNoteDbSecretKeySharedPrefereces", etxtSecretKey.getText().toString().trim());
                                editor.commit();
                                alertDialog.dismiss();

                                Toast.makeText(MainActivity.this, "تم اضافة البيانات الى البرنامج", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                } else if (fromWhere.equals("admin")) {

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("BusinessNoteDbSecretKeySharedPrefereces", dbCipherKey);
                    editor.commit();

                    mySqliteDB.UpdateDbCipherKey_dbCipherKey("uRuKrUbIsHcIpHeR", "user");

                    Toast.makeText(MainActivity.this, "تم اضافة البيانات الى البرنامج", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {

            Log.d("ImportDB", "error: " + e.toString());

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the selection didn't work
        if (requestCode == AddNewItemActivity.PICKUP_FILE_REQUEST) {

            if (resultCode != RESULT_OK) {
                // Exit without doing anything else
                return;
            } else {
                // Get the file's content URI from the incoming Intent
                Uri returnUri = data.getData();
                Log.d("ImportDB", "onActivityResult 1: "+ returnUri.getPath());
                //String filePath = getPath(returnUri);
                //getRealPathFromURI(returnUri);

                if (returnUri.getPath().startsWith("/root/storage/emulated/0/")) {
                    backUpFilePath_ForImporting = returnUri.getPath().substring(5);
                    Log.d("ImportDB", "onActivityResult 2: "+backUpFilePath_ForImporting);
                }else{

                    backUpFilePath_ForImporting = UriUtils.getPathFromUri(getApplicationContext(), returnUri);
                    Log.d("ImportDB", "onActivityResult 3: "+ backUpFilePath_ForImporting);
                }

                /*Log.d("ImportDB", returnUri.getPath());
                File file = new File(returnUri.getPath());//create path from uri

                Log.d("ImportDB", file.getPath());
                if (file.getPath().contains(":")) {

                    final String[] split = file.getPath().split(":");//split the path.
                    backUpFilePath_ForImporting = split[1];//assign it to a string(your choice).
                    Log.d("ImportDB", backUpFilePath_ForImporting);
                } else {
                    backUpFilePath_ForImporting = file.getPath();
                    Log.d("ImportDB", backUpFilePath_ForImporting);
                }*/
                //backUpFilePath_ForImporting = file.getPath();

                //get the extension of the picked file
                String extension = backUpFilePath_ForImporting.substring(backUpFilePath_ForImporting.lastIndexOf("."));

                //File tempFileToUpload = new File(filePath);
                //filePath = tempFileToUpload.getAbsolutePath();

                Log.d("ImportDB", "onActivityResult 4: "+extension);

                if (extension.equals(".db"))
                    if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this
                                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                                , permissionForImportingDbCode);
                    } else {

                        importDB(backUpFilePath_ForImporting);
                    }
                else
                    Toast.makeText(MainActivity.this, "الملف غير صحيح", Toast.LENGTH_SHORT).show();


                /*try {
                 *//*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 *//*
                    mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "File not found.");
                    return;
                }
                // Get a regular file descriptor for the file
                FileDescriptor fd = mInputPFD.getFileDescriptor();*/

            }
        }
    }


    //get path from uri .... 1st method   .....deprecated
    public String getPath(Uri uri) {

        String path = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    //get path from uri .... 2nd method   .....deprecated
    public String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getApplicationContext().getContentResolver().query(contentURI, null,
                null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            try {
                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                Log.d("ImportDB" , "getRealPathFromURI 1: " +result);
            } catch (Exception e) {
                Log.d("ImportDB" , "getRealPathFromURI 2: " +e.getMessage());

                result = "";
            }
            cursor.close();
        }
        return result;
    }


    //get path from uri .... 3rd method   .....deprecated
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {


        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {

                Log.d("ImportDB", "getPath 1: "+ uri.getAuthority());
                final String docId = DocumentsContract.getDocumentId(uri);
                Log.d("ImportDB", "getPath 1: "+ docId);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                Log.d("ImportDB", "getPath 2: "+ uri.getAuthority());
                final String id = DocumentsContract.getDocumentId(uri);
                String uriScheme = uri.getScheme();
                Log.d("ImportDB", "getPath 2: "+ uriScheme);
                Log.d("ImportDB", "getPath 2: "+ id);
                String uriPath = null;
//                if(uriScheme.toLowerCase().equals("content")){
//
//                    uriPath = getDataColumn(context, uri, null, null);
//                }
//                else{

                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    Log.d("ImportDB", "getPath 2: "+ contentUri.getPath());
                    uriPath = getDataColumn(context, contentUri, null, null);
//                }

                return uriPath;
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                Log.d("ImportDB", "getPath 3: "+ uri.getAuthority());
                final String docId = DocumentsContract.getDocumentId(uri);
                Log.d("ImportDB", "getPath 3: "+ docId);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.d("ImportDB", "getPath 4: "+ uri.getScheme());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.d("ImportDB", "getPath 5: "+ uri.getScheme());
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        catch (Exception ex){
            Log.d("ImportDB" , "error : " + ex.getMessage());
        }
        finally{
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    //end of 3rd method
}


//get path from uri .... 4th method ........used
class UriUtils {
    private static Uri contentUri = null;

    @SuppressLint("NewApi")
    public static String getPathFromUri(final Context context, final Uri uri) {
        // check here to is it KITKAT or new version
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));

                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }
                    }

                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            } else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
            if( Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
            {
                return getMediaFilePathForN(uri, context);
            }else
            {
                return getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String getMediaFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }


    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

}