package com.uruksys.businessnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MySqliteDB extends SQLiteOpenHelper {



    private static final String sqliteDbName = "BusinessNoteDb";

    public MySqliteDB(Context context) {
        super(context, sqliteDbName, null, 1);
        Log.d("sqlite_db", "called");

        SQLiteDatabase mySqlite = this.getWritableDatabase();
        //onCreate(mySqlite);
    }


    public void RecreateDb() {
        Log.d("sqlite_db", "Recreate db");

        SQLiteDatabase mySqlite = this.getWritableDatabase();
        onCreate(mySqlite);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.d("sqlite_db", "Created");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS dirHierarchy");
        sqLiteDatabase.execSQL("DROP TABLE  IF EXISTS items");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS dbCipherKey");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `dirHierarchy` (" +
                "dirId INTEGER NOT NULL primary key AUTOINCREMENT, " +
                "dirName varchar(100) NOT NULL, " +
                "dirCreatedDate DateTime NOT NULL, " +
                "parentDirId INTEGER NOT NULL, " +
                "parentDirName varchar(100) NOT NULL)");


        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `items` (" +
                "itemId INTEGER NOT NULL primary key AUTOINCREMENT , " +
                "itemName varchar(25) NOT NULL, " +
                "boughtPrice varchar(100) NOT NULL, " +
                "sellPrice varchar(100) NOT NULL, " +
                "supplier varchar(100) NOT NULL, " +
                "notes varchar(255) NOT NULL, " +
                "itemImage Blob NOT NULL, " +
                "attachment Blob, " +
                "parentDirId INTEGER NOT NULL)");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `dbCipherKey` ("+
                "id INTEGER NOT NULL primary key AUTOINCREMENT , "+
                "cipherKey varchar(100) NOT NULL, "+
                "fromWhere varchar(100))");


        ContentValues contentValues = new ContentValues();
        contentValues.put("cipherKey" ,"uRuKrUbIsHcIpHeR" );
        contentValues.put("fromWhere" , "user");
        sqLiteDatabase.insert( "dbCipherKey" , null , contentValues);

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public boolean UpdateDbCipherKey_dbCipherKey(String cipherKey , String fromWhere){

        Log.d("sqlite_db" , "UpdateDbCipherKey_dbCipherKey_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("cipherKey" , cipherKey);
        contentValues.put("fromWhere" , fromWhere);
        result = sqLiteDatabase.update("dbCipherKey" ,contentValues, "id =  '1'", null);


        Log.d("sqlite_db" , "update db cipher key read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public Cursor GetDbCipherKey_dbCipherKey(){

        Log.d("sqlite_db" , "GetDbCipherKey_dbCipherKey_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM dbCipherKey" , null);


        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetDbCipherKey_dbCipherKey__completed" );

        return cursor;
    }







    public Cursor GetDirContent_dirHierarchy(String currentDirId){
        Log.d("sqlite_db" , "GetDirContent_dirHierarchy_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From dirHierarchy WHERE parentDirId = ?"  , new String[]{currentDirId});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetDirContent_dirHierarchy_Completed");

        return cursor;
    }


    
    public Cursor GetParentDirId_dirHierarchy(String currentDirId){
        Log.d("sqlite_db" , "GetParentDirId_dirHierarchy_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From dirHierarchy WHERE dirId = ?"  , new String[]{currentDirId});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetParentDirId_dirHierarchy_Completed");

        return cursor;
    }



    public Long InsertNewFolder_dirHierarchy(String parentDirName , String parentDirId, String newDirName){

        Log.d("sqlite_db" , "InsertNewFolder_dirHierarchy_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("parentDirId" , parentDirId);
        contentValues.put("parentDirName" ,parentDirName );
        contentValues.put("dirName" ,newDirName );


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf.format(new Date());
        contentValues.put("dirCreatedDate" ,date );

        Long result = sqLiteDatabase.insert( "dirHierarchy" , null , contentValues);

        Log.d("sqlite_db" , "InsertNewFolder_dirHierarchy" + result);

        return result;
    }



    public boolean DeleteSelectedFolder_dirHierarchy(int dirId){

        Log.d("sqlite_db" , "DeleteSelectedFolder_dirHierarchy_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        int result = sqLiteDatabase.delete("dirHierarchy" , "dirId = "+ dirId , null);

        //action gives results only when deleting main dir , unlike secondary dir
        int result2 = sqLiteDatabase.delete("dirHierarchy" , "parentDirId = "+ dirId , null);

        //action gives results only when deleting secondary dir , unlike main dir
        int result3 = sqLiteDatabase.delete("items" , "parentDirId = "+ dirId , null);

        Log.d("sqlite_db" , "Delete Read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }


    public boolean MoveDir_DirHierarchy(int selectedDirId , int destinationDirId , String destinationDirName){

        Log.d("sqlite_db" , "MoveDir_DirHierarchy_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("parentDirId" , destinationDirId);
        contentValues.put("parentDirName" , destinationDirName);
        result = sqLiteDatabase.update("dirHierarchy" ,contentValues, "dirId = "+ selectedDirId , null);

        Log.d("sqlite_db" , "Move dir read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }


    public boolean UpdateDirName_DirHierarchy(int selectedDirId , String newDirName){

        Log.d("sqlite_db" , "UpdateDirName_DirHierarchy_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("dirName" , newDirName);
        result = sqLiteDatabase.update("dirHierarchy" ,contentValues, "dirId = "+ selectedDirId , null);

        //action gives results only when updating main dir , unlike secondary dir
        int result2 = sqLiteDatabase.update("dirHierarchy" ,contentValues, "parentDirId = "+ selectedDirId , null);

        Log.d("sqlite_db" , "update dir name read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public Cursor SearchForDir_dirHierarchy(String dirName){
        Log.d("sqlite_db" , "SearchForDir_dirHierarchy_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From dirHierarchy WHERE dirName like ?"  , new String[]{"%"+dirName+"%"});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "SearchForDir_dirHierarchy_Completed");

        return cursor;
    }


    //overloaded method
    public Cursor SearchForDir_dirHierarchy(String dirName , String parentDirId){
        Log.d("sqlite_db" , "SearchForDir_dirHierarchy_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From dirHierarchy WHERE dirName like ? AND parentDirId = ?"  , new String[]{"%"+dirName+"%", parentDirId});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "SearchForDir_dirHierarchy_Completed");

        return cursor;
    }






    public Cursor GetDirContent_items(String currentDirId){
        Log.d("sqlite_db" , "GetDirContent_items_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From items WHERE parentDirId = ?"  , new String[]{currentDirId});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetDirContent_items_Completed");

        return cursor;
    }



    public Long InsertNewItem_items(String itemName , String boughtPrice, String sellPrice, String supplier, String  notes,byte[] itemImage , String parentDirId , byte[] itemAttachment){

        Log.d("sqlite_db" , "InsertNewItem_items_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("itemName" , itemName);
        contentValues.put("boughtPrice" , boughtPrice);
        contentValues.put("sellPrice" , sellPrice);
        contentValues.put("itemImage" , itemImage);
        contentValues.put("parentDirId" , parentDirId);
        contentValues.put("supplier" , supplier);
        contentValues.put("notes" , notes);
        if(itemAttachment != null){

            contentValues.put("attachment" ,itemAttachment );
        }

        Long result = sqLiteDatabase.insert( "items" , null , contentValues);

        Log.d("sqlite_db" , "InsertNewItem_items" + result);

        return result;
    }



    public Cursor GetItemInfo_items(String itemId){

        Log.d("sqlite_db" , "GetItemInfo_items_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From items WHERE itemId = ?"  , new String[]{itemId});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetItemInfo_items_Completed");

        return cursor;
    }



    public Cursor GetSuppliers_items(){

        Log.d("sqlite_db" , "GetSuppliers_items_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select DISTINCT supplier From items"  , null);

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetSuppliers_items_Completed");

        return cursor;
    }



    public Cursor SearchForItemName_items(String itemName){
        Log.d("sqlite_db" , "SearchForItemName_items_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT items.itemId , items.itemName , items.boughtPrice , items.sellPrice, items.supplier , items.notes, items.itemImage , items.attachment , dirHierarchy.dirId AS secDirId , dirHierarchy.dirName AS secDirName , dirHierarchy.parentDirId AS mainDirId , dirHierarchy.parentDirName AS mainDirName FROM items LEFT JOIN dirHierarchy ON items.parentDirId = dirHierarchy.dirId WHERE itemName like ?"  , new String[]{"%"+itemName+"%"});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "SearchForItemName_items_Completed");

        return cursor;
    }



    public Cursor SearchForSupplier_items(String supplier){
        Log.d("sqlite_db" , "SearchForSupplier_items_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT items.itemId , items.itemName , items.boughtPrice , items.sellPrice, items.supplier , items.notes, items.itemImage , items.attachment , dirHierarchy.dirId AS secDirId , dirHierarchy.dirName AS secDirName , dirHierarchy.parentDirId AS mainDirId , dirHierarchy.parentDirName AS mainDirName FROM items LEFT JOIN dirHierarchy ON items.parentDirId = dirHierarchy.dirId WHERE supplier like ?"  , new String[]{"%"+supplier+"%"});

        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "SearchForSuppier_items_Completed");

        return cursor;
    }


    public boolean MoveItem_Items(int selectedItemId , int destinationDirId){

        Log.d("sqlite_db" , "MoveItem_Items_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("parentDirId" , destinationDirId);
        result = sqLiteDatabase.update("items" ,contentValues, "itemId = "+ selectedItemId , null);

        Log.d("sqlite_db" , "Move item read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }


    public boolean UpdateItemInfo_items(String selectedItemId, String newItemName, String newBoughtPrice, String newSellPrice, String newSupplier, String newNotes, byte[] newItemImage, byte[] newItemAttachment){

        Log.d("sqlite_db" , "UpdateItemInfo_items_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("itemName" , newItemName );
        contentValues.put("boughtPrice" , newBoughtPrice);
        contentValues.put("sellPrice" , newSellPrice);
        contentValues.put("itemImage" , newItemImage);
        contentValues.put("supplier" , newSupplier);
        contentValues.put("notes" , newNotes);
        if(newItemAttachment != null){

            contentValues.put("attachment" ,newItemAttachment );
        }
        result = sqLiteDatabase.update("items" ,contentValues, "itemId = "+ selectedItemId , null);

        Log.d("sqlite_db" , "update item Info read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public boolean DeleteSelectedItem_Items(int itemId){

        Log.d("sqlite_db" , "DeleteSelectedItem_Items_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        //action gives results only when deleting secondary dir , unlike main dir
        int result = sqLiteDatabase.delete("items" , "itemId = "+ itemId , null);

        Log.d("sqlite_db" , "Delete Read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }
}
