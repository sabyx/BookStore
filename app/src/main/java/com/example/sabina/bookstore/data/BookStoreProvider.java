package com.example.sabina.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.sabina.bookstore.data.BookStoreContract.ProductEntry;

public class BookStoreProvider extends ContentProvider {

    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private BookStoreDbHelper helper;

    static {
        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        helper = new BookStoreDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        validateName(values);
        validatePrice(values);
        validateQuantity(values);
        validateType(values);
        validateSupplierName(values);
        validateSupplierPhone(values);

        SQLiteDatabase database = helper.getWritableDatabase();
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private void validateSupplierPhone(ContentValues values) {
        String supplierPhone = values.getAsString(ProductEntry.PRODUCT_SUPPLIER_PHONE_NUMBER_COLUMN);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Product requires a supplier phone");
        }
    }

    private void validateSupplierName(ContentValues values) {
        String supplierName = values.getAsString(ProductEntry.PRODUCT_SUPPLIER_NAME_COLUMN);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }
    }

    private void validateType(ContentValues values) {
        int type = values.getAsInteger(ProductEntry.PRODUCT_TYPE_COLUMN);
        if (type != ProductEntry.PRODUCT_TYPE_BOOK
                && type != ProductEntry.PRODUCT_TYPE_MAGAZINE
                && type != ProductEntry.PRODUCT_TYPE_UNKNOWN) {
            throw new IllegalArgumentException("Product type can be 0, 1, 2");
        }
    }

    private void validateQuantity(ContentValues values) {
        int quantity = values.getAsInteger(ProductEntry.PRODUCT_QUANTITY_COLUMN);
        if (quantity < 0) {
            throw new IllegalArgumentException("Product requires a quantity");
        }
    }

    private void validatePrice(ContentValues values) {
        int price = values.getAsInteger(ProductEntry.PRODUCT_PRICE_COLUMN);
        if (price <= 0) {
            throw new IllegalArgumentException("Product requires a price");
        }
    }

    private void validateName(ContentValues values) {
        String name = values.getAsString(ProductEntry.PRODUCT_NAME_COLUMN);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = helper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateProduct(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductEntry.PRODUCT_NAME_COLUMN)) {
            validateName(values);
        }

        if (values.containsKey(ProductEntry.PRODUCT_PRICE_COLUMN)) {
            validatePrice(values);
        }

        if (values.containsKey(ProductEntry.PRODUCT_QUANTITY_COLUMN)) {
            validateQuantity(values);
        }

        if (values.containsKey(ProductEntry.PRODUCT_TYPE_COLUMN)) {
            validateType(values);
        }

        if (values.containsKey(ProductEntry.PRODUCT_SUPPLIER_NAME_COLUMN)) {
            validateSupplierName(values);
        }

        if (values.containsKey(ProductEntry.PRODUCT_SUPPLIER_PHONE_NUMBER_COLUMN)) {
            validateSupplierPhone(values);
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = helper.getWritableDatabase();
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
