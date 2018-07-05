package com.example.sabina.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.sabina.bookstore.data.BookCursorAdapter;
import com.example.sabina.bookstore.data.BookStoreContract;
import com.example.sabina.bookstore.data.BookStoreContract.ProductEntry;
import com.example.sabina.bookstore.data.BookStoreDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URI_LOADER = 0;

    private BookCursorAdapter adapter;
    private String[] projection = {
            ProductEntry._ID,
            ProductEntry.PRODUCT_NAME_COLUMN,
            ProductEntry.PRICE_COLUMN,
            ProductEntry.QUANTITY_COLUMN,
            ProductEntry.PRODUCT_TYPE_COLUMN,
            ProductEntry.SUPPLIER_NAME_COLUMN,
            ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN
    };

    ListView bookListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

//        Button saleButton = findViewById(R.id.list_item_sale);
//        saleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println("Button CLICKED");
////                decreaseQuantity();
//            }
//        });

        bookListView = findViewById(R.id.list_view_books);
        RelativeLayout emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        adapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(adapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(URI_LOADER, null, this);
    }

    private void decreaseQuantity() {

    }

//    private void insertProduct() {
//        SQLiteDatabase database = bookStoreDbHelper.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(ProductEntry.PRODUCT_NAME_COLUMN, "Are your lights on?");
//        values.put(ProductEntry.PRODUCT_TYPE_COLUMN, ProductEntry.PRODUCT_TYPE_BOOK);
//        values.put(ProductEntry.PRICE_COLUMN, 1399);
//        values.put(ProductEntry.QUANTITY_COLUMN, 5);
//        values.put(ProductEntry.SUPPLIER_NAME_COLUMN, "Dorset House Publishing");
//        values.put(ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN, "0700512368");
//
//        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
//    }
//
//    private void readProduct() {
//        SQLiteDatabase database = bookStoreDbHelper.getReadableDatabase();
//
//        String[] projection = {
//                ProductEntry._ID, ProductEntry.PRODUCT_NAME_COLUMN, ProductEntry.PRODUCT_TYPE_COLUMN,
//                ProductEntry.PRICE_COLUMN, ProductEntry.QUANTITY_COLUMN, ProductEntry.SUPPLIER_NAME_COLUMN,
//                ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN
//        };
//
//        try (Cursor cursor = database.query(ProductEntry.TABLE_NAME, projection, null, null,
//                null, null, null)) {
//            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
//            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_NAME_COLUMN);
//            int productTypeColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_TYPE_COLUMN);
//            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.PRICE_COLUMN);
//            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.QUANTITY_COLUMN);
//            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.SUPPLIER_NAME_COLUMN);
//            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN);
//
//            while (cursor.moveToNext()) {
//                int id = cursor.getInt(idColumnIndex);
//                String productName = cursor.getString(productNameColumnIndex);
//                int productType = cursor.getInt(productTypeColumnIndex);
//                int price = cursor.getInt(priceColumnIndex);
//                int quantity = cursor.getInt(quantityColumnIndex);
//                String supplierName = cursor.getString(supplierNameColumnIndex);
//                String supplierPhone = cursor.getString(supplierPhoneNumberColumnIndex);
//
//                System.out.println("Product: " + id + ", " + productName + ", " + getProductType(productType)
//                        + ", " + getPrice(price) + ", " + quantity + ", " + supplierName + ", " + supplierPhone);
//            }
//        }
//    }

//    private String getProductType(int productType){
//        switch (productType){
//            case ProductEntry.PRODUCT_TYPE_BOOK:
//                return getString(R.string.product_book);
//            default:
//                return getString(R.string.product_unknown);
//        }
//    }

//    private float getPrice(int price) {
//        return price/100;
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                return new CursorLoader(this, ProductEntry.CONTENT_URI, projection,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
