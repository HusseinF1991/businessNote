package com.uruksys.businessnote_admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchItemRecyclerAdapter extends RecyclerView.Adapter<SearchItemRecyclerAdapter.MyViewHolder> {

    Context myContext;
    ArrayList<ItemsModel> itemsSearchModelArrayList;
    int resource;

    public SearchItemRecyclerAdapter(Context context, int resource, ArrayList objects) {

        myContext = context;
        this.resource = resource;
        this.itemsSearchModelArrayList = objects;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtItemName, txtItemSellPrice, txtItemMainDirName, txtItemSecDirName;
        ImageView ImgViewItem;

        MyViewHolder(View view) {
            super(view);
            txtItemSellPrice = view.findViewById(R.id.txtItemSellPrice);
            txtItemName = view.findViewById(R.id.txtItemName);
            ImgViewItem = view.findViewById(R.id.ImgViewItem);
            txtItemMainDirName = view.findViewById(R.id.txtItemMainDirName);
            txtItemSecDirName = view.findViewById(R.id.txtItemSecDirName);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent intent = new Intent(myContext, ShowItemInfoActivity.class);
                    intent.putExtra("itemId", itemsSearchModelArrayList.get(getAdapterPosition()).getItemId());
                    myContext.startActivity(intent);
                }
            });


            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    itemLongClicked(getAdapterPosition());
                    return false;
                }
            });
        }
    }


    private void itemLongClicked(final int position) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(myContext);
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

                        int selectedItemId = itemsSearchModelArrayList.get(position).getItemId();


                        MySqliteDB mySqliteDB = new MySqliteDB(myContext, AgentsDataBasesActivity.selectedDbName);
                        mySqliteDB.DeleteSelectedItem_Items(selectedItemId);

                        itemsSearchModelArrayList.clear();

                        SharedPreferences sharedPref = myContext.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                        String dbSecretKey = sharedPref.getString("BusinessNoteAdminDbSecretKeySharedPrefereces", "");

                        if (SearchResultActivity.searchCategory.equals("itemName")) {
                            MySqliteDB mySqliteDB2 = new MySqliteDB(myContext, AgentsDataBasesActivity.selectedDbName);
                            Cursor c2 = mySqliteDB2.SearchForItemName_items(SearchResultActivity.keywordSearch);

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
                                                        boughtPrice, sellPrice, supplier, notes, itemImage, attachment);

                                        Log.d("DirActivity", itemsModel.toString());
                                        itemsSearchModelArrayList.add(itemsModel);
                                    } while (c2.moveToNext());
                                }
                                c2.close();
                            }
                        }
                        else{

                            MySqliteDB mySqliteDB2 = new MySqliteDB(myContext, AgentsDataBasesActivity.selectedDbName);
                            Cursor c2 = mySqliteDB2.SearchForSupplier_items(SearchResultActivity.keywordSearch);

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
                                                new ItemsModel(mainDirId, secDirId, itemId, decryptedMainDirName,decryptedSecDirName,  decryptedItemName, boughtPrice, sellPrice, supplier, notes, itemImage, attachment);

                                        Log.d("DirActivity", itemsModel.toString());
                                        itemsSearchModelArrayList.add(itemsModel);
                                    } while (c2.moveToNext());
                                }
                                c2.close();
                            }
                        }

                        notifyDataSetChanged();

                        dialogInterface.dismiss();
                    }
                });

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @NonNull
    @Override
    public SearchItemRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new SearchItemRecyclerAdapter.MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchItemRecyclerAdapter.MyViewHolder holder, int position) {

        ItemsModel itemsModel = itemsSearchModelArrayList.get(position);
        Log.d("logItemsAdapter", String.valueOf(itemsModel.getSellPrice()));
        holder.txtItemSellPrice.setText(String.valueOf(itemsModel.getSellPrice()));
        holder.txtItemName.setText(itemsModel.getItemName());
        holder.txtItemMainDirName.setText(itemsModel.getMainDirName());
        holder.txtItemSecDirName.setText(itemsModel.getSecDirName());
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(itemsModel.getItemImage(), 0, itemsModel.getItemImage().length);
        holder.ImgViewItem.setImageBitmap(bitmap);
    }


    @Override
    public int getItemCount() {
        return itemsSearchModelArrayList.size();
    }
}
