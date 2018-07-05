package com.example.sabina.bookstore.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.sabina.bookstore.R;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameText = view.findViewById(R.id.name);
        TextView priceText = view.findViewById(R.id.summary_price);
        TextView quantityText = view.findViewById(R.id.summary_quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(BookStoreContract.ProductEntry.PRICE_COLUMN));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(BookStoreContract.ProductEntry.QUANTITY_COLUMN));

        nameText.setText(name);
        priceText.setText(String.valueOf(price));
        quantityText.setText(String.valueOf(quantity));
    }
}
