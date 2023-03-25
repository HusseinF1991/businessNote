package com.uruksys.businessnote_admin;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class AgentsDataBasesActivity extends AppCompatActivity {

    ListView dataBasesListView;
    ArrayList<DataBasesModel> dataBasesModelArrayList = new ArrayList<>();
    Toolbar toolbar_AgentsDatabaseActivity;

    public static int dbListItemLongClickedPosition;

    public static String selectedDbName;
    public static String selectedDbSecretKey;

    String backUpFilePath_ForImporting = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agents_data_bases);


        // Find the toolbar view inside the activity layout
        toolbar_AgentsDatabaseActivity = (Toolbar) findViewById(R.id.toolbar_AgentsDatabaseActivity);
        toolbar_AgentsDatabaseActivity.setTitle("قواعد البيانات");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar_AgentsDatabaseActivity);

        dataBasesListView = findViewById(R.id.dataBasesListView);

    }


    @Override
    protected void onStart() {
        super.onStart();

        PopulateDatabases();
    }

    private void PopulateDatabases() {

        dataBasesModelArrayList.clear();

        MySqliteDB mySqliteDB = new MySqliteDB(this, "BusinessNoteDb_Main");
        Cursor c = mySqliteDB.GetAllDb_agentsDb();

        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    int dataBaseId = c.getInt(c.getColumnIndex("Id"));
                    String dbName = c.getString(c.getColumnIndex("dbName"));
                    String dbSecretKey = c.getString(c.getColumnIndex("dbSecretKey"));

                    DataBasesModel dataBasesModel =
                            new DataBasesModel(dataBaseId, dbName, dbSecretKey);

                    dataBasesModelArrayList.add(dataBasesModel);

                } while (c.moveToNext());
            }
            c.close();
        }


        DataBasesListAdapter dataBasesListAdapter = new DataBasesListAdapter(this,
                R.layout.databases_listview_item, dataBasesModelArrayList);
        dataBasesListView.setAdapter(dataBasesListAdapter);
        //ordersFromUpListView.setSelection(ordersFromAbove_adapter.getCount() - 1);
    }


    //delete db
    private void DeleteDataBase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsDataBasesActivity.this)
                .setMessage("هل ترغب بحذف قاعدة البيانات")
                .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AgentsDataBasesActivity.this.deleteDatabase(dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName());

                        MySqliteDB mySqliteDB = new MySqliteDB(AgentsDataBasesActivity.this, "BusinessNoteDb_Main");
                        mySqliteDB.DeleteDb_agentsDb(dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName());

                        PopulateDatabases();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //on context menu item selected of the db list view
    @Override
    public boolean onContextItemSelected(@NonNull final MenuItem item) {

        switch (item.getOrder()) {
            //export db copy
            case 1:

                ExportBySavingOrSendingDb();
                break;
            //delete db item
            case 2:

                DeleteDataBase();
                break;
        }
        return super.onContextItemSelected(item);
    }


    private void ExportBySavingOrSendingDb() {

        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.export_db_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsDataBasesActivity.this)
                .setView(view1);

        Button btnSaveNSendDbCopy = view1.findViewById(R.id.btnSaveNSendDbCopy);
        Button btnSaveCopy = view1.findViewById(R.id.btnSaveCopy);

        final AlertDialog alertDialog = builder.create();

        btnSaveCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((ActivityCompat.checkSelfPermission(AgentsDataBasesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(AgentsDataBasesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){

                    ActivityCompat.requestPermissions(AgentsDataBasesActivity.this
                            , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}
                            , AddNewItemActivity.WRITE_EXTERNAL_DATA_PERMISSION_CODE_EXPORT);
                } else {

                    exportDB();
                }
                alertDialog.dismiss();
            }
        });


        btnSaveNSendDbCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if ((ActivityCompat.checkSelfPermission(AgentsDataBasesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(AgentsDataBasesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){

                    ActivityCompat.requestPermissions(AgentsDataBasesActivity.this
                            , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}
                            , AddNewItemActivity.WRITE_EXTERNAL_DATA_PERMISSION_CODE_EXPORT);
                } else {
                    exportDB();

                    Intent myIntent = new Intent(Intent.ACTION_SEND);
                    myIntent.setType("application/octet-stream");
                    File dbFile = new File(getExternalFilesDir(null) + File.separator + "BusinessNoteDBs",
                            dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db");
                    //File dbFile = new File(Environment.getExternalStorageDirectory() + File.separator + "BusinessNoteDBs" + File.separator +
                    //        dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db");
                    //File dbFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                    //        dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db");


                    Uri uri = FileProvider.getUriForFile(AgentsDataBasesActivity.this, "com.uruksys.businessnote_admin.provider", dbFile);
                    Log.d("exportDb" , uri.toString());
                    myIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(myIntent, "Share using"));
                }
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AddNewItemActivity.WRITE_EXTERNAL_DATA_PERMISSION_CODE_EXPORT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "write to external storage granted", Toast.LENGTH_LONG).show();
                exportDB();
            } else {
                Toast.makeText(this, "write to external storage permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == AddNewItemActivity.WRITE_EXTERNAL_DATA_PERMISSION_CODE_IMPORT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "write to external storage granted", Toast.LENGTH_LONG).show();

                importDB(backUpFilePath_ForImporting);
            } else {
                Toast.makeText(this, "write to external storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void exportDB() {
        try {
            MySqliteDB mySqliteDB = new MySqliteDB(AgentsDataBasesActivity.this, dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName());
            mySqliteDB.UpdateDbCipherKey_dbCipherKey(dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbSecretKey() , "admin");


            File dbFile = new File(this.getDatabasePath(dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName()).getAbsolutePath());


            //save twice , step 1:
            //save in application folders com.uruksys.businessnote_admin
            FileInputStream fis = new FileInputStream(dbFile);
            File folder = new File(getExternalFilesDir(null) + File.separator + "BusinessNoteDBs");
            Log.d("exportDb", folder.getPath());
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();

                if (!success) {
                    success = folder.mkdirs();
                }

                Log.d("exportDb", "dir making process is : " + success);
            }
            if (success) {
                String outFileName = folder.getPath() + File.separator + dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db";
                //String outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                //       dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db";
                Log.d("exportDb", outFileName);

                // Open the empty db as the output stream
                OutputStream output = new FileOutputStream(outFileName);

                //Transfer bytes from the input file to the output file
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
                    String outFileName2 = folder2.getPath()  + File.separator + dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db";
                    //String outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                    //        dataBasesModelArrayList.get(dbListItemLongClickedPosition).getDbName() + ".db";
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.agents_db_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Import_AgentsDbActivity) {

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("application/octet-stream");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, AddNewItemActivity.PICKUP_FILE_REQUEST);
        } else if (item.getItemId() == R.id.CreateNewDb_AgentsDbActivity) {

            CreateNewDb();

        } else if (item.getItemId() == R.id.LogOut_AgentsDbActivity) {


            SharedPreferences sharedPref = this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("BusinessNoteAdminKeepSignedInSharedPrefereces", false);
            editor.commit();

            this.finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void CreateNewDb() {
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.create_new_db_info_dialog, null);
        final EditText etxtSecretKey = view1.findViewById(R.id.etxtDbSecretKey);
        final EditText etxtDbName = view1.findViewById(R.id.etxtDbName);
        Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
        final Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsDataBasesActivity.this)
                .setView(view1);
        final AlertDialog alertDialog = builder.create();

        btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etxtSecretKey.getText().toString().trim().length() < 6) {
                    Toast.makeText(AgentsDataBasesActivity.this, "رمز الشفرة يجب ان لا يقل عن 6 مراتب", Toast.LENGTH_SHORT).show();

                } else if (etxtDbName.getText().toString().trim().equals("")) {
                    Toast.makeText(AgentsDataBasesActivity.this, "ادخل اسم قاعدة البيانات رجاءا", Toast.LENGTH_SHORT).show();

                } else {
                    for (DataBasesModel dataBasesModel : dataBasesModelArrayList) {
                        if (dataBasesModel.getDbName().equals(etxtDbName.getText().toString().trim())) {
                            Toast.makeText(AgentsDataBasesActivity.this, "الاسم المدخل موجود مسبقا", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    //create new db file
                    MySqliteDB mySqliteDB = new MySqliteDB(AgentsDataBasesActivity.this, etxtDbName.getText().toString().trim());
                    mySqliteDB.UpdateDbCipherKey_dbCipherKey(etxtSecretKey.getText().toString().trim() , "admin");

                    //add the new db name and secret key to the main db table
                    MySqliteDB mySqliteDB2 = new MySqliteDB(AgentsDataBasesActivity.this, "BusinessNoteDb_Main");
                    mySqliteDB2.InsertNewDb_agentsDb(etxtDbName.getText().toString().trim(), etxtSecretKey.getText().toString().trim());


                    Toast.makeText(AgentsDataBasesActivity.this, "تم انشاء قاعدة بيانات جديدة", Toast.LENGTH_SHORT).show();


                    SharedPreferences sharedPref = AgentsDataBasesActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("BusinessNoteAdminDbSecretKeySharedPrefereces", etxtSecretKey.getText().toString().trim());
                    editor.commit();

                    alertDialog.dismiss();


                    PopulateDatabases();
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


    private void importDB(final String filePath) {
        /*
         * Try to open the file for "read" access using the
         * returned URI. If the file isn't found, write to the
         * error log and return.
         */
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.import_new_db_name_dialog, null);
        final EditText etxtDbName = view1.findViewById(R.id.etxtDbName);
        Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
        final Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsDataBasesActivity.this)
                .setView(view1);
        final AlertDialog alertDialog = builder.create();

        btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etxtDbName.getText().toString().trim().equals("")) {
                    Toast.makeText(AgentsDataBasesActivity.this, "ادخل اسم قاعدة البيانات رجاءا", Toast.LENGTH_SHORT).show();

                } else {
                    for (DataBasesModel dataBasesModel : dataBasesModelArrayList) {
                        if (dataBasesModel.getDbName().equals(etxtDbName.getText().toString().trim())) {
                            Toast.makeText(AgentsDataBasesActivity.this, "الاسم المدخل موجود مسبقا", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    //create new db file
                    MySqliteDB mySqliteDB = new MySqliteDB(AgentsDataBasesActivity.this, etxtDbName.getText().toString().trim());


                    try {
                        File sd = Environment.getExternalStorageDirectory();
                        File data1 = Environment.getDataDirectory();

                        if (sd.canWrite()) {
                            //String  currentDBPath = "//data//" + "PackageName" + "//databases//" + "DatabaseName";
                            String currentDBPath = AgentsDataBasesActivity.this.getDatabasePath(etxtDbName.getText().toString().trim()).getAbsolutePath();
                            File currentDB = new File(currentDBPath);
                            Log.d("ImportDB", currentDB.toString());

                            String backupDBPath = filePath;
                            File backupDB = new File(backupDBPath);
                            Log.d("ImportDB", backupDB.toString());

                            FileChannel src = new FileInputStream(backupDB).getChannel();
                            FileChannel dst = new FileOutputStream(currentDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                            Toast.makeText(AgentsDataBasesActivity.this, "تم اضافة البيانات الى البرنامج", Toast.LENGTH_SHORT).show();

                            Log.d("ImportDB", backupDB.toString());

                            String dbCipherKey = null;
                            Cursor c = mySqliteDB.GetDbCipherKey_dbCipherKey();
                            if (c.getCount() > 0) {
                                if (c.moveToFirst()) {
                                    do {
                                        dbCipherKey = c.getString(c.getColumnIndex("cipherKey"));
                                    } while (c.moveToNext());
                                }
                                c.close();
                            }

                            //add the new db name and secret key to the main db table
                            MySqliteDB mySqliteDB2 = new MySqliteDB(AgentsDataBasesActivity.this, "BusinessNoteDb_Main");
                            mySqliteDB2.InsertNewDb_agentsDb(etxtDbName.getText().toString().trim(), dbCipherKey);

                            SharedPreferences sharedPref = AgentsDataBasesActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("BusinessNoteAdminDbSecretKeySharedPrefereces", dbCipherKey);
                            editor.commit();

                            alertDialog.dismiss();


                            PopulateDatabases();
                        }
                    } catch (Exception e) {

                        Log.d("ImportDB", "error: " + e.toString());
                    }
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
                }else {
                    backUpFilePath_ForImporting = UriUtils.getPathFromUri(getApplicationContext(), returnUri);
                    Log.d("ImportDB", "onActivityResult 3: " + backUpFilePath_ForImporting);
                }


                //get the extension of the picked file
                //String extension = backUpFilePath_ForImporting.substring(backUpFilePath_ForImporting.lastIndexOf("."));
                String extension = FilenameUtils.getExtension(backUpFilePath_ForImporting);
                Log.d("ImportDB", "onActivityResult 4:"+"extension " + extension);

                if (extension.equals("db")) {
                    if ((ActivityCompat.checkSelfPermission(AgentsDataBasesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(
                                AgentsDataBasesActivity.this
                                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                                , AddNewItemActivity.WRITE_EXTERNAL_DATA_PERMISSION_CODE_IMPORT);
                    } else {

                        importDB(backUpFilePath_ForImporting);
                    }
                } else
                    Toast.makeText(AgentsDataBasesActivity.this, "الملف غير صحيح", Toast.LENGTH_SHORT).show();
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
}


class DataBasesModel {

    private int dataBaseId;
    private String dbName, dbSecretKey;

    public DataBasesModel(int dbId, String dbName, String dbSecretKey) {
        this.dataBaseId = dbId;
        this.dbName = dbName;
        this.dbSecretKey = dbSecretKey;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbSecretKey() {
        return dbSecretKey;
    }

    public int getDataBaseId() {
        return dataBaseId;
    }
}





//get path from uri ............used
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