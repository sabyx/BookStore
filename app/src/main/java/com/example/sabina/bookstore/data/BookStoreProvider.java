package com.example.sabina.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BookStoreProvider extends ContentProvider {

    private static final int BOOKS = 100;
    private static final int BOOKS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private BookStoreDbHelper helper;

    static {
        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKS + "/#", BOOKS_ID);
    }

    @Override
    public boolean onCreate() {
        helper = new BookStoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookStoreContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOOKS_ID:
                selection = BookStoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(BookStoreContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookStoreContract.ProductEntry.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return BookStoreContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        SQLiteDatabase database = helper.getWritableDatabase();

        validateName(values);
        validatePrice(values);
        validateQuantity(values);
        validateType(values);
        validateSupplierName(values);
        validateSupplierPhone(values);

        long id = database.insert(BookStoreContract.ProductEntry.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private void validateSupplierPhone(ContentValues values) {
        String supplierPhone = values.getAsString(BookStoreContract.ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Product requires a supplier phone");
        }
    }

    private void validateSupplierName(ContentValues values) {
        String supplierName = values.getAsString(BookStoreContract.ProductEntry.SUPPLIER_NAME_COLUMN);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }
    }

    private void validateType(ContentValues values) {
        int type = values.getAsInteger(BookStoreContract.ProductEntry.PRODUCT_TYPE_COLUMN);
        if (type != BookStoreContract.ProductEntry.PRODUCT_TYPE_BOOK
                && type != BookStoreContract.ProductEntry.PRODUCT_TYPE_MAGAZINE
                && type != BookStoreContract.ProductEntry.PRODUCT_TYPE_UNKNOWN) {
            throw new IllegalArgumentException("Product type can be 0, 1, 2");
        }
    }

    private void validateQuantity(ContentValues values) {
        int quantity = values.getAsInteger(BookStoreContract.ProductEntry.QUANTITY_COLUMN);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Product requires a quantity");
        }
    }

    private void validatePrice(ContentValues values) {
        int price = values.getAsInteger(BookStoreContract.ProductEntry.PRICE_COLUMN);
        if (price <= 0) {
            throw new IllegalArgumentException("Product requires a price");
        }
    }

    private void validateName(ContentValues values) {
        String name = values.getAsString(BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = helper.getWritableDatabase();

        int rowsDeleted = 0;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOKS_ID:
                selection = BookStoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BookStoreContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
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
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsUpdated = 0;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsUpdated = updateProduct(uri, values, selection, selectionArgs);
                break;
            case BOOKS_ID:
                selection = BookStoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = updateProduct(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = helper.getWritableDatabase();

        if (values.containsKey(BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN)) {
            validateName(values);
        }

        if (values.containsKey(BookStoreContract.ProductEntry.PRICE_COLUMN)) {
            validatePrice(values);
        }

        if (values.containsKey(BookStoreContract.ProductEntry.QUANTITY_COLUMN)) {
            validateQuantity(values);
        }

        if (values.containsKey(BookStoreContract.ProductEntry.PRODUCT_TYPE_COLUMN)) {
            validateType(values);
        }

        if (values.containsKey(BookStoreContract.ProductEntry.SUPPLIER_NAME_COLUMN)) {
            validateSupplierName(values);
        }

        if (values.containsKey(BookStoreContract.ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN)) {
            validateSupplierPhone(values);
        }

        return database.update(BookStoreContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
