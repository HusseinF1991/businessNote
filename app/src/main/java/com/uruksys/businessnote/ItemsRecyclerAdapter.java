package com.uruksys.businessnote;

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

public class ItemsRecyclerAdapter extends RecyclerView.Adapter<ItemsRecyclerAdapter.MyViewHolder> implements View.OnClickListener, View.OnLongClickListener  {

    Context myContext;
    //ArrayList<ItemsModel> itemsModelArrayList;
    int resource;

    public ItemsRecyclerAdapter(Context context, int resource, ArrayList objects) {
        myContext = context;
        this.resource = resource;
        //itemsModelArrayList = objects;
    }



    @Override
    public void onClick(View view) {
        int position = DirActivity.ItemsRecyclerView.getChildLayoutPosition(view);
        Intent intent = new Intent(myContext , ShowItemInfoActivity.class);
        intent.putExtra("itemId" , DirActivity.itemsModelArrayList.get(position).getItemId());
        myContext.startActivity(intent);
    }



    @Override
    public boolean onLongClick(final View view) {

        final int position = DirActivity.ItemsRecyclerView.getChildLayoutPosition(view);
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

                        int selectedItemId = DirActivity.itemsModelArrayList.get(position).getItemId();


                        MySqliteDB mySqliteDB = new MySqliteDB(myContext);
                        mySqliteDB.DeleteSelectedItem_Items(selectedItemId);

                        DirActivity.selectedSecondaryDirId= String.valueOf(DirActivity.secondaryDirsModelArrayList.get(DirActivity.selectedSecondaryDirPosition).getDirId());
                        DirActivity.selectedSecondaryDirName = DirActivity.secondaryDirsModelArrayList
                                .get(DirActivity.selectedSecondaryDirPosition).getDirName();
                        DirActivity.itemsModelArrayList.clear();

                        SharedPreferences sharedPref = myContext.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                        String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

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
                                                    ,itemId, DirActivity.selectedMainDirName, DirActivity.selectedSecondaryDirName , itemName ,
                                                    boughtPrice , sellPrice, supplier , notes,itemImage , attachment);

                                    DirActivity.itemsModelArrayList.add(itemsModel);
                                } while (c.moveToNext());
                            }
                            c.close();
                        }

                        notifyDataSetChanged();

                        dialogInterface.dismiss();
                    }
                });

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return false;
    }



    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtItemName , txtItemSellPrice;
        ImageView ImgViewItem;

        MyViewHolder(View view) {
            super(view);
            txtItemSellPrice = view.findViewById(R.id.txtItemSellPrice);
            txtItemName = view.findViewById(R.id.txtItemName);
            ImgViewItem = view.findViewById(R.id.ImgViewItem);
        }
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        return new MyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ItemsModel itemsModel = DirActivity.itemsModelArrayList.get(position);
        Log.d("logItemsAdapter" , String.valueOf(itemsModel.getSellPrice()));
        holder.txtItemSellPrice.setText(String.valueOf(itemsModel.getSellPrice()));
        holder.txtItemName.setText(itemsModel.getItemName());
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(itemsModel.getItemImage(), 0, itemsModel.getItemImage().length);
        holder.ImgViewItem.setImageBitmap(bitmap);
    }



    @Override
    public int getItemCount() {
        return DirActivity.itemsModelArrayList.size();
    }
}
