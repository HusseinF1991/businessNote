package com.uruksys.businessnote_admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DataBasesListAdapter extends ArrayAdapter implements View.OnLongClickListener, View.OnClickListener {


    Context myContext;
    ArrayList<DataBasesModel> dataBasesModelArrayList;
    int resource;

    public DataBasesListAdapter(Context context, int resource, ArrayList objects) {
        super(context, resource, objects);

        myContext = context;
        this.resource = resource;
        this.dataBasesModelArrayList = objects;
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag(R.string.DataBasesListAdapterTag1);
        DataBasesModel dataBasesModel = dataBasesModelArrayList.get(position);

        AgentsDataBasesActivity.selectedDbName = dataBasesModel.getDbName();
        AgentsDataBasesActivity.selectedDbSecretKey = dataBasesModel.getDbSecretKey();

        SharedPreferences sharedPref = myContext.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("BusinessNoteAdminDbSecretKeySharedPrefereces", dataBasesModel.getDbSecretKey());
        editor.commit();

        Intent intent = new Intent(myContext, DirActivity.class);
        myContext.startActivity(intent);
    }


    @Override
    public boolean onLongClick(View view) {

        AgentsDataBasesActivity.dbListItemLongClickedPosition = (Integer) view.getTag(R.string.DataBasesListAdapterTag1);
        return false;
    }


    class ViewHolder {
        TextView txtDataBaseName;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView;
        ViewHolder holder;
        DataBasesModel dataBasesModel = dataBasesModelArrayList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(myContext);
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();

            holder.txtDataBaseName = convertView.findViewById(R.id.txtDataBaseName);

            itemView = convertView;
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
            itemView = convertView;
        }

        holder.txtDataBaseName.setText(dataBasesModel.getDbName());

        itemView.setTag(R.string.DataBasesListAdapterTag1 , position);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                //set the choices(items) for the context menu
                contextMenu.setHeaderTitle("أختر احد الخيارات");
                MenuItem exportDbItem = contextMenu.add(Menu.NONE, 1, 1, "استخراج نسخة");
                MenuItem deleteDbItem = contextMenu.add(Menu.NONE, 2, 2, "حذف");
            }
        });
        return itemView;
    }
}
