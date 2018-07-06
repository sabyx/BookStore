package com.example.sabina.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sabina.bookstore.data.BookStoreContract.ProductEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameText = (TextView) view.findViewById(R.id.name);
        TextView priceText = (TextView) view.findViewById(R.id.summary_price);
        final TextView quantityText = (TextView) view.findViewById(R.id.summary_quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.PRODUCT_NAME_COLUMN));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.PRODUCT_PRICE_COLUMN));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.PRODUCT_QUANTITY_COLUMN));

        nameText.setText(name);
        priceText.setText(String.valueOf(price));
        quantityText.setText(String.valueOf(quantity));

        Button saleButton = (Button) view.findViewById(R.id.list_item_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseQuantity(cursor, quantityText, context);
            }
        });
    }

    private void decreaseQuantity(Cursor cursor, TextView quantityText, Context context) {
        final int quantity = Integer.parseInt(quantityText.getText().toString());
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getLong(idColumnIndex));

        if (quantity > 0) {
            int reducedQuantity = quantity - 1;
            ContentValues values = new ContentValues();
            values.put(ProductEntry.PRODUCT_QUANTITY_COLUMN, reducedQuantity);

            int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(context, context.getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                quantityText.setText(String.valueOf(reducedQuantity));
                Toast.makeText(context, context.getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.editor_update_product_quantity_low),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
