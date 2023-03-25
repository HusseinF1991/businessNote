package com.uruksys.businessnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SecondaryDirsRecyclerAdapter extends RecyclerView.Adapter<SecondaryDirsRecyclerAdapter.MyViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    Context myContext;
    //ArrayList<SecondaryDirsModel> secondaryDirsModelArrayList;
    //ArrayList<ItemsModel> itemsModelArrayList = new ArrayList<>();
    int resource;

    public SecondaryDirsRecyclerAdapter(Context context, int resource, ArrayList objects) {

        myContext = context;
        this.resource = resource;
        //secondaryDirsModelArrayList = objects;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtDirName;

        MyViewHolder(View view) {
            super(view);
            txtDirName = view.findViewById(R.id.txtDirName);
        }
    }


    @Override
    public void onClick(View view) {

        MyViewHolder viewHolder1 = (MyViewHolder) DirActivity.SecondaryDirRecyclerView
                .findViewHolderForAdapterPosition(DirActivity.selectedSecondaryDirPosition);
        if(viewHolder1 != null)
            viewHolder1.txtDirName.setBackgroundColor(Color.TRANSPARENT);

        DirActivity.selectedSecondaryDirPosition = DirActivity.SecondaryDirRecyclerView.getChildLayoutPosition(view);

        MyViewHolder viewHolder2 = (MyViewHolder) DirActivity.SecondaryDirRecyclerView
                .findViewHolderForAdapterPosition(DirActivity.selectedSecondaryDirPosition);
        viewHolder2.txtDirName.setBackgroundColor(Color.parseColor("#e8eaf6"));

        DirActivity.selectedSecondaryDirId= String.valueOf(DirActivity.secondaryDirsModelArrayList.get(DirActivity.selectedSecondaryDirPosition).getDirId());
        DirActivity.selectedSecondaryDirName = DirActivity.secondaryDirsModelArrayList
                .get(DirActivity.selectedSecondaryDirPosition).getDirName();
        DirActivity.itemsModelArrayList.clear();

        SharedPreferences sharedPref = myContext.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
        String dbSecretKey = sharedPref.getString("BusinessNoteDbSecretKeySharedPrefereces", "");

        MySqliteDB mySqliteDB = new MySqliteDB(myContext);
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
                                    boughtPrice , sellPrice , supplier , notes,itemImage , attachment);

                    DirActivity.itemsModelArrayList.add(itemsModel);
                } while (c.moveToNext());
            }
            c.close();
        }

        ItemsRecyclerAdapter itemsRecyclerAdapter = new ItemsRecyclerAdapter(myContext,
                R.layout.items_recyclerview_item, DirActivity.itemsModelArrayList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(myContext , 2);
        DirActivity.ItemsRecyclerView.setLayoutManager(mLayoutManager);
        DirActivity.ItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DirActivity.ItemsRecyclerView.setAdapter(itemsRecyclerAdapter);

    }


    @Override
    public boolean onLongClick(View view) {
        DirActivity.dirSecondaryRecyclerItemLongClickedPosition  = DirActivity.SecondaryDirRecyclerView.getChildLayoutPosition(view);
        DirActivity.dirMainRecyclerItemLongClickedPosition = -1;
        return false;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                //set the choices(items) for the context menu
                contextMenu.setHeaderTitle("أختر احد الخيارات");
                MenuItem deleteItem = contextMenu.add(Menu.NONE, 1, 1, "حذف");
                MenuItem renameItem = contextMenu.add(Menu.NONE, 2, 2, "تعديل الاسم");
            }
        });

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull SecondaryDirsRecyclerAdapter.MyViewHolder holder, int position) {
        SecondaryDirsModel secondaryDirsModel = DirActivity.secondaryDirsModelArrayList.get(position);
        holder.txtDirName.setText(secondaryDirsModel.getDirName());

        if(position ==  DirActivity.selectedSecondaryDirPosition){
            holder.txtDirName.setBackgroundColor(Color.parseColor("#e8eaf6"));
        }
    }


    @Override
    public int getItemCount() {
        return DirActivity.secondaryDirsModelArrayList.size();
    }
}
