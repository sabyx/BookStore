package com.example.sabina.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.sabina.bookstore.data.BookStoreContract.ProductEntry;

public class BookStoreDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1;

    public BookStoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE =  "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.PRODUCT_NAME_COLUMN + " TEXT NOT NULL, "
                + ProductEntry.PRODUCT_TYPE_COLUMN + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.PRODUCT_PRICE_COLUMN + " INTEGER NOT NULL, "
                + ProductEntry.PRODUCT_QUANTITY_COLUMN + " INTEGER DEFAULT 1, "
                + ProductEntry.PRODUCT_SUPPLIER_NAME_COLUMN + " TEXT NOT NULL, "
                + ProductEntry.PRODUCT_SUPPLIER_PHONE_NUMBER_COLUMN + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
