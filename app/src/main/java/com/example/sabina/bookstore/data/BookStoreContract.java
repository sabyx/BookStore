package com.example.sabina.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookStoreContract {

    public static final String CONTENT_AUTHORITY = "com.example.sabina.books";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    public BookStoreContract() { }

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        public static final String TABLE_NAME = "products";

        public static final String _id = BaseColumns._ID;
        public static final String PRODUCT_NAME_COLUMN = "product_name";
        public static final String PRODUCT_TYPE_COLUMN = "product_type";
        public static final String PRICE_COLUMN = "price";
        public static final String QUANTITY_COLUMN = "quantity";
        public static final String SUPPLIER_NAME_COLUMN = "supplier_name";
        public static final String SUPPLIER_PHONE_NUMBER_COLUMN = "supplier_phone_number";

        public static final int PRODUCT_TYPE_UNKNOWN = 0;
        public static final int PRODUCT_TYPE_BOOK = 1;
        public static final int PRODUCT_TYPE_MAGAZINE = 2;

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
    }
}
