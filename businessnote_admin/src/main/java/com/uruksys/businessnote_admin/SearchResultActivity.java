package com.uruksys.businessnote_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    RecyclerView recyclerViewSearchResult;
    ArrayList<MainDirsModel> mainDirsSearchModelArrayList = new ArrayList<>();
    ArrayList<SecondaryDirsModel> secDirsSearchModelArrayList = new ArrayList<>();
    ArrayList<ItemsModel> itemsSearchModelArrayList = new ArrayList<>();
    TextView txtNotifyMoved_SearchActivity;
    Button btnSearch;
    EditText etxtSearchKeyWord;
    RadioButton rbSearchForItemName, rbSearchForSupplier;


    public static String keywordSearch , searchCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recyclerViewSearchResult = findViewById(R.id.recyclerViewSearchResult);
        txtNotifyMoved_SearchActivity = findViewById(R.id.txtNotifyMoved_SearchActivity);

        btnSearch = findViewById(R.id.btnSearch);
        etxtSearchKeyWord = findViewById(R.id.etxtSearchKeyWord);
        rbSearchForItemName = findViewById(R.id.rbSearchForItemName);
        rbSearchForSupplier = findViewById(R.id.rbSearchForSupplier);


        // Find the toolbar view inside the activity layout
        Toolbar searchResultActivityToolbar = (Toolbar) findViewById(R.id.toolbar_SearchResultActivity);
        searchResultActivityToolbar.setTitle("بحث");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(searchResultActivityToolbar);


        CreateListenerOnSearchBtn();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        SearchForKeyword();
    }



    private void CreateListenerOnSearchBtn() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                keywordSearch = etxtSearchKeyWord.getText().toString().trim();
                if (rbSearchForItemName.isChecked()) {
                    searchCategory = "itemName";
                }
                else{

                    searchCategory = "supplier";
                }
                SearchForKeyword();
            }
        });
    }



    private void SearchForKeyword() {

        SharedPreferences sharedPref = SearchResultActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
        String dbSecretKey = sharedPref.getString("BusinessNoteAdminDbSecretKeySharedPrefereces", "");

        itemsSearchModelArrayList.clear();
        if (rbSearchForItemName.isChecked()) {

            MySqliteDB mySqliteDB2 = new MySqliteDB(this,AgentsDataBasesActivity.selectedDbName);
            Cursor c2 = mySqliteDB2.SearchForItemName_items(keywordSearch);

            if (c2.getCount() > 0) {
                if (c2.moveToFirst()) {
                    do {
                        int mainDirId = c2.getInt(c2.getColumnIndex("mainDirId"));
                        int secDirId = c2.getInt(c2.getColumnIndex("secDirId"));
                        int itemId = c2.getInt(c2.getColumnIndex("itemId"));
                        String decryptedMainDirName = AES.decrypt(c2.getString(c2.getColumnIndex("mainDirName")), dbSecretKey);
                        String decryptedSecDirName = AES.decrypt(c2.getString(c2.getColumnIndex("secDirName")), dbSecretKey);
                        String decryptedItemName = c2.getString(c2.getColumnIndex("itemName"));
                        String boughtPrice = AES.decrypt(c2.getString(c2.getColumnIndex("boughtPrice")), dbSecretKey);
                        String sellPrice = AES.decrypt(c2.getString(c2.getColumnIndex("sellPrice")), dbSecretKey);
                        String supplier = c2.getString(c2.getColumnIndex("supplier"));
                        String notes = c2.getString(c2.getColumnIndex("notes"));
                        byte[] itemImage = c2.getBlob(c2.getColumnIndex("itemImage"));
                        byte[] attachment = c2.getBlob(c2.getColumnIndex("attachment"));
                        ItemsModel itemsModel =
                                new ItemsModel(mainDirId, secDirId, itemId, decryptedMainDirName, decryptedSecDirName, decryptedItemName,
                                        boughtPrice, sellPrice, supplier,notes, itemImage, attachment);

                        Log.d("DirActivity123", itemsModel.toString());
                        itemsSearchModelArrayList.add(itemsModel);
                    } while (c2.moveToNext());
                }
                c2.close();
            }


            SearchItemRecyclerAdapter searchItemRecyclerAdapter = new SearchItemRecyclerAdapter(this,
                    R.layout.search_items_recyclerview_item, itemsSearchModelArrayList);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
            recyclerViewSearchResult.setLayoutManager(mLayoutManager);
            recyclerViewSearchResult.setItemAnimator(new DefaultItemAnimator());
            recyclerViewSearchResult.setAdapter(searchItemRecyclerAdapter);
        }else {


            MySqliteDB mySqliteDB2 = new MySqliteDB(SearchResultActivity.this,AgentsDataBasesActivity.selectedDbName);
            Cursor c2 = mySqliteDB2.SearchForSupplier_items(keywordSearch);

            if (c2.getCount() > 0) {
                if (c2.moveToFirst()) {
                    do {
                        int mainDirId = c2.getInt(c2.getColumnIndex("mainDirId"));
                        int secDirId = c2.getInt(c2.getColumnIndex("secDirId"));
                        int itemId = c2.getInt(c2.getColumnIndex("itemId"));
                        String decryptedMainDirName = AES.decrypt(c2.getString(c2.getColumnIndex("mainDirName")), dbSecretKey);
                        String decryptedSecDirName = AES.decrypt(c2.getString(c2.getColumnIndex("secDirName")), dbSecretKey);
                        String decryptedItemName = c2.getString(c2.getColumnIndex("itemName"));
                        String boughtPrice = AES.decrypt(c2.getString(c2.getColumnIndex("boughtPrice")), dbSecretKey);
                        String sellPrice = AES.decrypt(c2.getString(c2.getColumnIndex("sellPrice")), dbSecretKey);
                        String supplier = c2.getString(c2.getColumnIndex("supplier"));
                        String notes = c2.getString(c2.getColumnIndex("notes"));
                        byte[] itemImage = c2.getBlob(c2.getColumnIndex("itemImage"));
                        byte[] attachment = c2.getBlob(c2.getColumnIndex("attachment"));
                        ItemsModel itemsModel =
                                new ItemsModel(mainDirId, secDirId, itemId, decryptedMainDirName, decryptedSecDirName, decryptedItemName, boughtPrice, sellPrice , supplier , notes, itemImage, attachment);

                        Log.d("DirActivity123", itemsModel.toString());
                        itemsSearchModelArrayList.add(itemsModel);
                    } while (c2.moveToNext());
                }
                c2.close();
            }


            SearchItemRecyclerAdapter searchItemRecyclerAdapter = new SearchItemRecyclerAdapter(SearchResultActivity.this,
                    R.layout.search_items_recyclerview_item, itemsSearchModelArrayList);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(SearchResultActivity.this, 3);
            recyclerViewSearchResult.setLayoutManager(mLayoutManager);
            recyclerViewSearchResult.setItemAnimator(new DefaultItemAnimator());
            recyclerViewSearchResult.setAdapter(searchItemRecyclerAdapter);
        }

    }
}