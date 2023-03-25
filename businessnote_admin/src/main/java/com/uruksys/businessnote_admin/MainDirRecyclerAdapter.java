package com.uruksys.businessnote_admin;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainDirRecyclerAdapter extends RecyclerView.Adapter<MainDirRecyclerAdapter.MyViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    Context myContext;
    //ArrayList<MainDirsModel> mainDirsModelArrayList;
    //ArrayList<SecondaryDirsModel> secondaryDirsModelArrayList = new ArrayList<>();
    int resource;

    public MainDirRecyclerAdapter(Context context, int resource, ArrayList objects) {

        myContext = context;
        this.resource = resource;
        //mainDirsModelArrayList = objects;
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

        DirActivity.selectedSecondaryDirPosition = 0;

        MyViewHolder viewHolder1 = (MyViewHolder) DirActivity.MainDirRecyclerView
                .findViewHolderForAdapterPosition(DirActivity.selectedMainDirPosition);
        if(viewHolder1 != null)
            viewHolder1.txtDirName.setBackgroundColor(Color.TRANSPARENT);

        DirActivity.selectedMainDirPosition = DirActivity.MainDirRecyclerView.getChildLayoutPosition(view);

        MyViewHolder viewHolder2 = (MyViewHolder) DirActivity.MainDirRecyclerView
                .findViewHolderForAdapterPosition(DirActivity.selectedMainDirPosition);
        viewHolder2.txtDirName.setBackgroundColor(Color.parseColor("#e8eaf6"));

        DirActivity.selectedMainDirId = String.valueOf(DirActivity.mainDirsModelArrayList.get(DirActivity.selectedMainDirPosition).getDirId());
        DirActivity.selectedMainDirName = DirActivity.mainDirsModelArrayList.get(DirActivity.selectedMainDirPosition).getDirName();
        DirActivity.secondaryDirsModelArrayList.clear();

        SharedPreferences sharedPref = myContext.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
        String dbSecretKey = sharedPref.getString("BusinessNoteAdminDbSecretKeySharedPrefereces", "");

        MySqliteDB mySqliteDB = new MySqliteDB(myContext,AgentsDataBasesActivity.selectedDbName);
        Cursor c = mySqliteDB.GetDirContent_dirHierarchy(DirActivity.selectedMainDirId);


        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    int dirId = c.getInt(c.getColumnIndex("dirId"));
                    String dirName = AES.decrypt(c.getString(c.getColumnIndex("dirName")), dbSecretKey);
                    int parentDirId = c.getInt(c.getColumnIndex("parentDirId"));
                    String parentDirName = AES.decrypt(c.getString(c.getColumnIndex("parentDirName")), dbSecretKey);
                    SecondaryDirsModel secondaryDirsModel =
                            new SecondaryDirsModel(dirId, dirName , parentDirId , parentDirName);

                    DirActivity.secondaryDirsModelArrayList.add(secondaryDirsModel);
                } while (c.moveToNext());
            }
            c.close();
        }

        SecondaryDirsRecyclerAdapter secondaryDirsRecyclerAdapter = new SecondaryDirsRecyclerAdapter(myContext,
                R.layout.secondary_dir_recyclerview_item, DirActivity.secondaryDirsModelArrayList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(myContext);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        DirActivity.SecondaryDirRecyclerView.setLayoutManager(mLayoutManager);
        DirActivity.SecondaryDirRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DirActivity.SecondaryDirRecyclerView.setAdapter(secondaryDirsRecyclerAdapter);


        //populate the items of the first secondary dir
        if(DirActivity.secondaryDirsModelArrayList.size() > 0 ){

            DirActivity.selectedSecondaryDirId= String.valueOf(DirActivity.secondaryDirsModelArrayList.get(0).getDirId());
            DirActivity.selectedSecondaryDirName = DirActivity.secondaryDirsModelArrayList.get(0).getDirName();
            DirActivity.itemsModelArrayList.clear();


            Cursor c2 = mySqliteDB.GetDirContent_items(DirActivity.selectedSecondaryDirId);


            if (c2.getCount() > 0) {
                if (c2.moveToFirst()) {
                    do {
                        int itemId = c2.getInt(c2.getColumnIndex("itemId"));
                        String itemName = c2.getString(c2.getColumnIndex("itemName"));
                        String boughtPrice = AES.decrypt(c2.getString(c2.getColumnIndex("boughtPrice")), dbSecretKey);
                        String sellPrice = AES.decrypt(c2.getString(c2.getColumnIndex("sellPrice")), dbSecretKey);
                        String supplier = c2.getString(c2.getColumnIndex("supplier"));
                        String notes = c2.getString(c2.getColumnIndex("notes"));
                        byte[] itemImage = c2.getBlob(c2.getColumnIndex("itemImage"));
                        byte[] attachment = c2.getBlob(c2.getColumnIndex("attachment"));
                        ItemsModel itemsModel =
                                new ItemsModel(Integer.parseInt(DirActivity.selectedMainDirId), Integer.parseInt(DirActivity.selectedSecondaryDirId)
                                        ,itemId, DirActivity.selectedMainDirName, DirActivity.selectedSecondaryDirName , itemName ,
                                        boughtPrice , sellPrice,supplier,notes,itemImage , attachment);

                        DirActivity.itemsModelArrayList.add(itemsModel);
                    } while (c2.moveToNext());
                }
                c2.close();
            }

            ItemsRecyclerAdapter itemsRecyclerAdapter = new ItemsRecyclerAdapter(myContext,
                    R.layout.items_recyclerview_item, DirActivity.itemsModelArrayList);
            RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(myContext , 3);
            DirActivity.ItemsRecyclerView.setLayoutManager(mLayoutManager2);
            DirActivity.ItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            DirActivity.ItemsRecyclerView.setAdapter(itemsRecyclerAdapter);

        }
    }


    @Override
    public boolean onLongClick(View view) {
        DirActivity.dirMainRecyclerItemLongClickedPosition = DirActivity.MainDirRecyclerView.getChildLayoutPosition(view);
        DirActivity.dirSecondaryRecyclerItemLongClickedPosition =-1;
        return false;
    }


    @NonNull
    @Override
    public MainDirRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

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
    public void onBindViewHolder(@NonNull MainDirRecyclerAdapter.MyViewHolder holder, int position) {

        MainDirsModel mainDirsModel = DirActivity.mainDirsModelArrayList.get(position);
        holder.txtDirName.setText(mainDirsModel.getDirName());
        if(position == DirActivity.selectedMainDirPosition){
            holder.txtDirName.setBackgroundColor(Color.parseColor("#e8eaf6"));
        }
    }


    @Override
    public int getItemCount() { return DirActivity.mainDirsModelArrayList.size(); }
}
