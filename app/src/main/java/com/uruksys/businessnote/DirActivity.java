package com.uruksys.businessnote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DirActivity extends AppCompatActivity  {


    public static RecyclerView MainDirRecyclerView, SecondaryDirRecyclerView, ItemsRecyclerView;
    FloatingActionButton fabAddNewItem;
    Button btnAddMainDir, btnAddSecondaryDir;
    public static ArrayList<MainDirsModel> mainDirsModelArrayList = new ArrayList<>();
    public static ArrayList<SecondaryDirsModel> secondaryDirsModelArrayList = new ArrayList<>();
    public static ArrayList<ItemsModel> itemsModelArrayList = new ArrayList<>();

    public static String selectedMainDirId, selectedSecondaryDirId;
    public static String selectedMainDirName, selectedSecondaryDirName;

    public static int selectedMainDirPosition = 0, selectedSecondaryDirPosition = 0;

    public static int dirMainRecyclerItemLongClickedPosition, dirSecondaryRecyclerItemLongClickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MainDirRecyclerView = findViewById(R.id.MainDirRecyclerView);
        SecondaryDirRecyclerView = findViewById(R.id.SecondaryDirRecyclerView);
        ItemsRecyclerView = findViewById(R.id.ItemsRecyclerView);
        btnAddMainDir = findViewById(R.id.btnAddMainDir);
        btnAddSecondaryDir = findViewById(R.id.btnAddSecondaryDir);
        fabAddNewItem = findViewById(R.id.fabAddNewItem);

        // Find the toolbar view inside the activity layout
        Toolbar dirActivityToolbar = (Toolbar) findViewById(R.id.toolbar_DirActivity);
        dirActivityToolbar.setTitle("المنتجات");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(dirActivityToolbar);

        GetMainDirsInRecyclerView();
        AddNewMainDir();
        AddNewSecondaryDir();
        AddNewItem();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        selectedMainDirPosition = 0;
        selectedSecondaryDirPosition = 0;
    }



    @Override
    protected void onRestart() {
        super.onRestart();


        SelectFirstMainNSecDir();
    }


    //populate the main dir in the list view
    private void GetMainDirsInRecyclerView() {
        mainDirsModelArrayList.clear();

        MySqliteDB mySqliteDB = new MySqliteDB(this);
        Cursor c = mySqliteDB.GetDirContent_dirHierarchy("0");

        SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
        String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {

                    String decryptedDirName = AES.decrypt(c.getString(c.getColumnIndex("dirName")), dbSecretKey);

                    //in case the db secret key is wrong the app promote the user to insert the right key
                    if (decryptedDirName == null) {

                        InsertDbSecretKey();
                        return;
                    }

                    int dirId = c.getInt(c.getColumnIndex("dirId"));
                    MainDirsModel mainDirsModel =
                            new MainDirsModel(dirId, decryptedDirName);

                    Log.d("DirActivity", mainDirsModel.toString());
                    mainDirsModelArrayList.add(mainDirsModel);

                } while (c.moveToNext());
            }
            c.close();


            btnAddSecondaryDir.setEnabled(true);
            fabAddNewItem.setEnabled(true);
        }
        else{     //disable add sec dir btn and add item btn if there is no main dir

            btnAddSecondaryDir.setEnabled(false);
            fabAddNewItem.setEnabled(false);
        }

        MainDirRecyclerAdapter mainDirRecyclerAdapter = new MainDirRecyclerAdapter(this,
                R.layout.main_dir_recyclerview_item, mainDirsModelArrayList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        DirActivity.MainDirRecyclerView.setLayoutManager(mLayoutManager);
        DirActivity.MainDirRecyclerView.setItemAnimator(new DefaultItemAnimator());
        MainDirRecyclerView.setAdapter(mainDirRecyclerAdapter);


        SelectFirstMainNSecDir();
    }



    private void SelectFirstMainNSecDir() {

        //populate the secondary dir of the first main dir
        if (mainDirsModelArrayList.size() > 0) {

            DirActivity.selectedMainDirId = String.valueOf(mainDirsModelArrayList.get(selectedMainDirPosition).getDirId());
            DirActivity.selectedMainDirName = mainDirsModelArrayList.get(selectedMainDirPosition).getDirName();
            secondaryDirsModelArrayList.clear();

            SharedPreferences sharedPref = this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

            MySqliteDB mySqliteDB = new MySqliteDB(this);
            Cursor c = mySqliteDB.GetDirContent_dirHierarchy(DirActivity.selectedMainDirId);


            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        int dirId = c.getInt(c.getColumnIndex("dirId"));
                        String dirName = AES.decrypt(c.getString(c.getColumnIndex("dirName")), dbSecretKey);
                        int parentDirId = c.getInt(c.getColumnIndex("parentDirId"));
                        String parentDirName = AES.decrypt(c.getString(c.getColumnIndex("parentDirName")), dbSecretKey);
                        SecondaryDirsModel secondaryDirsModel =
                                new SecondaryDirsModel(dirId, dirName, parentDirId, parentDirName);

                        secondaryDirsModelArrayList.add(secondaryDirsModel);
                    } while (c.moveToNext());
                }
                c.close();
            }

            SecondaryDirsRecyclerAdapter secondaryDirsRecyclerAdapter = new SecondaryDirsRecyclerAdapter(this,
                    R.layout.secondary_dir_recyclerview_item, secondaryDirsModelArrayList);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            DirActivity.SecondaryDirRecyclerView.setLayoutManager(mLayoutManager);
            DirActivity.SecondaryDirRecyclerView.setItemAnimator(new DefaultItemAnimator());
            DirActivity.SecondaryDirRecyclerView.setAdapter(secondaryDirsRecyclerAdapter);
        }


        //populate the items of the first secondary dir
        if (secondaryDirsModelArrayList.size() > 0) {

            DirActivity.selectedSecondaryDirId = String.valueOf(DirActivity.secondaryDirsModelArrayList.get(selectedSecondaryDirPosition).getDirId());
            DirActivity.selectedSecondaryDirName = DirActivity.secondaryDirsModelArrayList.get(selectedSecondaryDirPosition).getDirName();
            DirActivity.itemsModelArrayList.clear();

            SharedPreferences sharedPref = this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

            MySqliteDB mySqliteDB = new MySqliteDB(this);
            Cursor c = mySqliteDB.GetDirContent_items(DirActivity.selectedSecondaryDirId);


            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        int itemId = c.getInt(c.getColumnIndex("itemId"));
                        String itemName = c.getString(c.getColumnIndex("itemName"));
                        String boughtPrice = AES.decrypt(c.getString(c.getColumnIndex("boughtPrice")), dbSecretKey);
                        String sellPrice = AES.decrypt(c.getString(c.getColumnIndex("sellPrice")), dbSecretKey);
                        String supplier = c.getString(c.getColumnIndex("supplier"));
                        String notes = c.getString(c.getColumnIndex("notes"));
                        byte[] itemImage = c.getBlob(c.getColumnIndex("itemImage"));
                        byte[] attachment = c.getBlob(c.getColumnIndex("attachment"));
                        ItemsModel itemsModel =
                                new ItemsModel(Integer.parseInt(DirActivity.selectedMainDirId), Integer.parseInt(DirActivity.selectedSecondaryDirId)
                                        , itemId, DirActivity.selectedMainDirName, DirActivity.selectedSecondaryDirName, itemName,
                                        boughtPrice, sellPrice , supplier , notes , itemImage, attachment);

                        DirActivity.itemsModelArrayList.add(itemsModel);
                    } while (c.moveToNext());
                }
                c.close();
            }

            ItemsRecyclerAdapter itemsRecyclerAdapter = new ItemsRecyclerAdapter(this,
                    R.layout.items_recyclerview_item, DirActivity.itemsModelArrayList);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            DirActivity.ItemsRecyclerView.setLayoutManager(mLayoutManager);
            DirActivity.ItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            DirActivity.ItemsRecyclerView.setAdapter(itemsRecyclerAdapter);

        }
    }


    private void AddNewMainDir() {
        btnAddMainDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LayoutInflater inflater = getLayoutInflater();
                View view2 = inflater.inflate(R.layout.create_new_folder_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(DirActivity.this);
                builder.setView(view2);

                final EditText etxtNewFolderNameDialog = view2.findViewById(R.id.etxtNewFolderNameDialog);
                Button btnCreateNewFolderDialog = view2.findViewById(R.id.btnCreateNewFolderDialog);
                Button btnCancelDialog = view2.findViewById(R.id.btnCancelDialog);

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                btnCreateNewFolderDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etxtNewFolderNameDialog.getText().toString().trim().equals("")) {
                            Toast.makeText(DirActivity.this, "ادخل اسم الصنف رجاءا ", Toast.LENGTH_SHORT).show();
                        } else {

                            SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            //create main dir
                            String encryptedDirName = AES.encrypt("root", dbSecretKey);
                            String encryptedNewDirName = AES.encrypt(etxtNewFolderNameDialog.getText().toString().trim(), dbSecretKey);

                            MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                            long lastRowId = mySqliteDB.InsertNewFolder_dirHierarchy(encryptedDirName, "0", encryptedNewDirName);


                            //create secondary dir with the same main dir name
                            mySqliteDB.InsertNewFolder_dirHierarchy(encryptedNewDirName, String.valueOf(lastRowId), encryptedNewDirName);


                            GetMainDirsInRecyclerView();
                            Toast.makeText(DirActivity.this, "تم انشاء صنف جديد ", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    }
                });

                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }


    private void AddNewSecondaryDir() {
        btnAddSecondaryDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = getLayoutInflater();
                View view2 = inflater.inflate(R.layout.create_new_folder_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(DirActivity.this);
                builder.setView(view2);

                final EditText etxtNewFolderNameDialog = view2.findViewById(R.id.etxtNewFolderNameDialog);
                Button btnCreateNewFolderDialog = view2.findViewById(R.id.btnCreateNewFolderDialog);
                Button btnCancelDialog = view2.findViewById(R.id.btnCancelDialog);

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                btnCreateNewFolderDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etxtNewFolderNameDialog.getText().toString().trim().equals("")) {
                            Toast.makeText(DirActivity.this, "ادخل اسم الصنف رجاءا ", Toast.LENGTH_SHORT).show();
                        } else {

                            SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            String encryptedDirName = AES.encrypt(selectedMainDirName, dbSecretKey);
                            String encryptedNewDirName = AES.encrypt(etxtNewFolderNameDialog.getText().toString().trim(), dbSecretKey);

                            MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                            mySqliteDB.InsertNewFolder_dirHierarchy(encryptedDirName, selectedMainDirId, encryptedNewDirName);

                            GetSecondaryDirsInRecyclerView();
                            Toast.makeText(DirActivity.this, "تم انشاء صنف جديد ", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    }
                });

                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }


    private void AddNewItem() {
        fabAddNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DirActivity.this, AddNewItemActivity.class);
                intent.putExtra("parentDirId", selectedSecondaryDirId);
                startActivity(intent);
            }
        });
    }


    //populate the secondary dir in the list view
    private void GetSecondaryDirsInRecyclerView() {
        secondaryDirsModelArrayList.clear();

        MySqliteDB mySqliteDB = new MySqliteDB(this);
        Cursor c = mySqliteDB.GetDirContent_dirHierarchy(selectedMainDirId);

        SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
        String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");


        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    int dirId = c.getInt(c.getColumnIndex("dirId"));
                    String dirName = AES.decrypt(c.getString(c.getColumnIndex("dirName")), dbSecretKey);
                    int parentDirId = c.getInt(c.getColumnIndex("parentDirId"));
                    String parentDirName = AES.decrypt(c.getString(c.getColumnIndex("parentDirName")), dbSecretKey);
                    SecondaryDirsModel secondaryDirsModel =
                            new SecondaryDirsModel(dirId, dirName, parentDirId, parentDirName);

                    secondaryDirsModelArrayList.add(secondaryDirsModel);
                } while (c.moveToNext());
            }
            c.close();
        }

        SecondaryDirsRecyclerAdapter secondaryDirsRecyclerAdapter = new SecondaryDirsRecyclerAdapter(getApplicationContext(),
                R.layout.secondary_dir_recyclerview_item, secondaryDirsModelArrayList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        SecondaryDirRecyclerView.setLayoutManager(mLayoutManager);
        SecondaryDirRecyclerView.setItemAnimator(new DefaultItemAnimator());
        SecondaryDirRecyclerView.setAdapter(secondaryDirsRecyclerAdapter);
    }


    //in case the db secret key is wrong the app promote the user to insert the right key
    private void InsertDbSecretKey() {

        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.db_secret_key_dialog, null);
        TextView txtDialogTitle = view1.findViewById(R.id.txtDialogTitle);
        txtDialogTitle.setText("رمز الشفرة المدخل غير صحيح , ادخل الرمز الصحيح رجاءا");
        final EditText etxtSecretKey = view1.findViewById(R.id.etxtSecretKey);
        Button btnSubmitSecretKey = view1.findViewById(R.id.btnSubmitSecretKey);
        Button btnCancelDialog = view1.findViewById(R.id.btnCancelDialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(DirActivity.this)
                .setView(view1);
        final AlertDialog alertDialog = builder.create();

        btnSubmitSecretKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etxtSecretKey.getText().toString().trim().length() < 6) {
                    Toast.makeText(DirActivity.this, "رمز الشفرة يجب ان لا يقل عن 6 مراتب", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("BusinessNoteDbSecretKeySharedPrefereces", etxtSecretKey.getText().toString().trim());
                    editor.commit();

                    Toast.makeText(DirActivity.this, "تم تثبيت رمز الشفرة الجديد", Toast.LENGTH_SHORT).show();
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

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DirActivity.this.finish();
            }
        });
        alertDialog.show();
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {


        if (dirSecondaryRecyclerItemLongClickedPosition != -1)
            item = ContextMenuPressedOfSecondaryDir(item);
        else if (dirMainRecyclerItemLongClickedPosition != -1)
            item = ContextMenuPressedOfMainDir(item);

        return super.onContextItemSelected(item);
    }


    private MenuItem ContextMenuPressedOfSecondaryDir(MenuItem item) {

        switch (item.getOrder()) {
            //delete one item
            case 1:


                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("تحذير")
                        .setMessage("هل ترغب بحذف الملف")
                        .setNegativeButton("كلا", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(secondaryDirsModelArrayList.size() > 1){

                                    int selectedDirId = secondaryDirsModelArrayList.get(dirSecondaryRecyclerItemLongClickedPosition).getDirId();


                                    MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                                    mySqliteDB.DeleteSelectedFolder_dirHierarchy(selectedDirId);

                                    selectedSecondaryDirPosition = 0;

                                    GetSecondaryDirsInRecyclerView();
                                    SelectFirstMainNSecDir();
                                }
                                else{
                                    Toast.makeText(DirActivity.this , "لا يمكن الحذف , يوجد صنف واحد فقط" , Toast.LENGTH_SHORT).show();
                                }

                                dialogInterface.dismiss();
                            }
                        });

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;

            //Rename dir
            case 2:

                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.create_new_folder_dialog, null);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setView(view);

                final EditText etxtNewFolderNameDialog = view.findViewById(R.id.etxtNewFolderNameDialog);
                Button btnCreateNewFolderDialog = view.findViewById(R.id.btnCreateNewFolderDialog);
                btnCreateNewFolderDialog.setText("تعديل");
                Button btnCancelDialog = view.findViewById(R.id.btnCancelDialog);


                final int selectedDirOrItemId = secondaryDirsModelArrayList.get(dirSecondaryRecyclerItemLongClickedPosition).getDirId();
                etxtNewFolderNameDialog.setText(secondaryDirsModelArrayList.get(dirSecondaryRecyclerItemLongClickedPosition).getDirName());


                final AlertDialog alertDialog2 = builder2.create();
                alertDialog2.show();

                btnCreateNewFolderDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etxtNewFolderNameDialog.getText().toString().trim().equals("")) {
                            Toast.makeText(DirActivity.this, "ادخل اسم الجديد رجاءا ", Toast.LENGTH_SHORT).show();
                        } else {

                            SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                            String encryptedDirName = AES.encrypt(etxtNewFolderNameDialog.getText().toString().trim(), dbSecretKey);
                            mySqliteDB.UpdateDirName_DirHierarchy(selectedDirOrItemId, encryptedDirName);

                            GetSecondaryDirsInRecyclerView();
                            Toast.makeText(DirActivity.this, "تم التعديل ", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog2.dismiss();
                    }
                });

                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog2.dismiss();
                    }
                });
                break;
        }

        return item;
    }


    private MenuItem ContextMenuPressedOfMainDir(MenuItem item) {

        switch (item.getOrder()) {
            //delete one item
            case 1:


                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("تحذير")
                        .setMessage("هل ترغب بحذف الملف")
                        .setNegativeButton("كلا", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(mainDirsModelArrayList.size() > 1 ){

                                    int selectedDirId;
                                    selectedDirId = mainDirsModelArrayList.get(dirMainRecyclerItemLongClickedPosition).getDirId();

                                    MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                                    mySqliteDB.DeleteSelectedFolder_dirHierarchy(selectedDirId);
                                    selectedMainDirPosition = 0;
                                    selectedSecondaryDirPosition = 0;

                                    //populate the main dirs in recycler view
                                    GetMainDirsInRecyclerView();
                                }
                                else{
                                    Toast.makeText(DirActivity.this , "لا يمكن الحذف , يوجد صنف واحد فقط" , Toast.LENGTH_SHORT).show();
                                }

                                dialogInterface.dismiss();
                            }
                        });

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;

            //Rename dir
            case 2:

                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.create_new_folder_dialog, null);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setView(view);

                final EditText etxtNewFolderNameDialog = view.findViewById(R.id.etxtNewFolderNameDialog);
                Button btnCreateNewFolderDialog = view.findViewById(R.id.btnCreateNewFolderDialog);
                btnCreateNewFolderDialog.setText("تعديل");
                Button btnCancelDialog = view.findViewById(R.id.btnCancelDialog);


                final int selectedDirOrItemId = mainDirsModelArrayList.get(dirMainRecyclerItemLongClickedPosition).getDirId();
                etxtNewFolderNameDialog.setText(mainDirsModelArrayList.get(dirMainRecyclerItemLongClickedPosition).getDirName());


                final AlertDialog alertDialog2 = builder2.create();
                alertDialog2.show();

                btnCreateNewFolderDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etxtNewFolderNameDialog.getText().toString().trim().equals("")) {
                            Toast.makeText(DirActivity.this, "ادخل اسم الجديد رجاءا ", Toast.LENGTH_SHORT).show();
                        } else {

                            SharedPreferences sharedPref = DirActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

                            MySqliteDB mySqliteDB = new MySqliteDB(DirActivity.this);
                            String encryptedDirName = AES.encrypt(etxtNewFolderNameDialog.getText().toString().trim(), dbSecretKey);
                            mySqliteDB.UpdateDirName_DirHierarchy(selectedDirOrItemId, encryptedDirName);

                            //populate the main dirs in recycler view
                            GetMainDirsInRecyclerView();

                            Toast.makeText(DirActivity.this, "تم التعديل ", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog2.dismiss();
                    }
                });

                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog2.dismiss();
                    }
                });
                break;
        }
        return item;
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dir_activity_menu, menu);

        /*MenuItem menuItem = menu.findItem(R.id.search_DirActivity);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("ابحث عن منتج");
        searchView.setOnQueryTextListener(this);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if(item.getItemId() == R.id.search_DirActivity){

            Intent intent = new Intent(DirActivity.this, SearchResultActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    /* @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.trim().equals("")) {
            Toast.makeText(DirActivity.this, "ادخل رمز البحث رجاءا ", Toast.LENGTH_SHORT).show();
        } else {


            Intent intent = new Intent(DirActivity.this, SearchResultActivity.class);
            intent.putExtra("keywordSearch", query);

            startActivity(intent);
        }
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }*/
}




class MainDirsModel {

    private int dirId;
    private String dirName;

    public MainDirsModel(int dirId, String dirName) {
        this.dirId = dirId;
        this.dirName = dirName;
    }


    public int getDirId() {
        return dirId;
    }

    public String getDirName() {
        return dirName;
    }

    public String toString() {
        return dirId + " " + dirName;
    }
}


class SecondaryDirsModel {

    private int dirId, parentDirId;
    private String dirName, parentDirName;

    public SecondaryDirsModel(int dirId, String dirName, int parentDirId, String parentDirName) {
        this.dirId = dirId;
        this.dirName = dirName;
        this.parentDirId = parentDirId;
        this.parentDirName = parentDirName;
    }


    public int getDirId() {
        return dirId;
    }

    public String getDirName() {
        return dirName;
    }

    public int getParentDirId() {
        return parentDirId;
    }

    public String getParentDirName() {
        return parentDirName;
    }

    public String toString() {
        return dirId + " " + dirName;
    }
}


class ItemsModel {

    private int mainDirId, secDirId, itemId;
    private String mainDirName, secDirName, itemName , supplier , notes;
    private byte[] itemImage, attachment;
    private String boughtPrice, sellPrice;


    public ItemsModel(int mainDirId, int secDirId, int itemId, String mainDirName, String secDirName, String itemName,
                      String boughtPrice, String sellPrice,String supplier ,String notes , byte[] itemImage, byte[] attachment) {
        this.itemId = itemId;
        this.mainDirId = mainDirId;
        this.secDirId = secDirId;
        this.itemName = itemName;
        this.mainDirName = mainDirName;
        this.secDirName = secDirName;
        this.boughtPrice = boughtPrice;
        this.sellPrice = sellPrice;
        this.itemImage = itemImage;
        this.attachment = attachment;
        this.supplier = supplier;
        this.notes = notes;
    }

    public int getMainDirId() {
        return mainDirId;
    }

    public int getSecDirId() {
        return secDirId;
    }

    public String getMainDirName() {
        return mainDirName;
    }

    public String getSecDirName() {
        return secDirName;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public byte[] getItemImage() {
        return itemImage;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public String getBoughtPrice() {
        return boughtPrice;
    }

    public String getSellPrice() {
        return sellPrice;
    }


    public String getSupplier() {
        return supplier;
    }

    public String getNotes() {
        return notes;
    }
}
