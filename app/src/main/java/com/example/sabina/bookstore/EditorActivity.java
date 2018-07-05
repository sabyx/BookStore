package com.example.sabina.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sabina.bookstore.data.BookCursorAdapter;
import com.example.sabina.bookstore.data.BookStoreContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URI_LOADER = 0;

    private EditText nameText;
    private EditText priceText;
    private EditText quantityText;
    private Spinner typeSpinner;
    private EditText supplierNameText;
    private EditText supplierPhoneText;

    private Uri currentProductUri;
    private BookCursorAdapter adapter;
    private Intent intent;

    private String[] projection = {
            BookStoreContract.ProductEntry._ID,
            BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN,
            BookStoreContract.ProductEntry.PRICE_COLUMN,
            BookStoreContract.ProductEntry.QUANTITY_COLUMN,
            BookStoreContract.ProductEntry.PRODUCT_TYPE_COLUMN,
            BookStoreContract.ProductEntry.SUPPLIER_NAME_COLUMN,
            BookStoreContract.ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN
    };

    private boolean productHasChanged = false;
    private int productType = BookStoreContract.ProductEntry.PRODUCT_TYPE_BOOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        intent = getIntent();
        currentProductUri = intent.getData();
        adapter = new BookCursorAdapter(this, null);

        setEditorTitle();
        initViews();
        initTouchListener();
        setupSpinner();

        getLoaderManager().initLoader(URI_LOADER, null, this);
    }

    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        typeSpinner.setAdapter(genderSpinnerAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.product_book))) {
                        productType = BookStoreContract.ProductEntry.PRODUCT_TYPE_BOOK;
                    } else if (selection.equals(getString(R.string.product_magazine))) {
                        productType = BookStoreContract.ProductEntry.PRODUCT_TYPE_MAGAZINE;
                    } else {
                        productType = BookStoreContract.ProductEntry.PRODUCT_TYPE_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productType = BookStoreContract.ProductEntry.PRODUCT_TYPE_UNKNOWN;
            }
        });
    }

    private void setEditorTitle() {
        if (currentProductUri == null) {
            setTitle(R.string.editor_view_add_product);
        } else {
            setTitle(R.string.editor_view_edit_product);
        }
    }

    private void initTouchListener() {
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                productHasChanged = true;
                return false;
            }
        };
        nameText.setOnTouchListener(touchListener);
        priceText.setOnTouchListener(touchListener);
        quantityText.setOnTouchListener(touchListener);
        typeSpinner.setOnTouchListener(touchListener);
        supplierNameText.setOnTouchListener(touchListener);
        supplierPhoneText.setOnTouchListener(touchListener);
    }

    private void initViews() {
        nameText = findViewById(R.id.edit_product_name);
        priceText = findViewById(R.id.edit_product_price);
        quantityText = findViewById(R.id.edit_product_quantity);
        typeSpinner = findViewById(R.id.spinner_type);
        supplierNameText = findViewById(R.id.edit_supplier_name);
        supplierPhoneText = findViewById(R.id.edit_supplier_phone);
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        showDiscardDialog();
    }

    private void showDiscardDialog() {
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boolean close = saveProduct();
                if (close) {
                    finish();
                }
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveProduct() {
        String name = nameText.getText().toString().trim();
        int price = 0;
        int quantity = 0;
        String supplierName = supplierNameText.getText().toString().trim();
        String supplierPhone = supplierPhoneText.getText().toString().trim();

        String priceString = priceText.getText().toString().trim();
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }

        String quantityString = quantityText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        boolean continueOperation = true;
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(supplierName)
                || price <= 0 || quantity <= 0 || TextUtils.isEmpty(supplierPhone)) {
            continueOperation = false;
        }

        if (continueOperation) {
            ContentValues values = new ContentValues();
            values.put(BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN, name);
            values.put(BookStoreContract.ProductEntry.PRICE_COLUMN, price);
            values.put(BookStoreContract.ProductEntry.QUANTITY_COLUMN, quantity);
            values.put(BookStoreContract.ProductEntry.SUPPLIER_NAME_COLUMN, supplierName);
            values.put(BookStoreContract.ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN, supplierPhone);

            int rowsAffected = 0;

            if (currentProductUri == null) {
                Uri newUri = getContentResolver().insert(BookStoreContract.ProductEntry.CONTENT_URI, values);
                if (newUri != null) {
                    rowsAffected = 1;
                }
            } else {
                rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            }

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            showDiscardDialog();
        }

        return continueOperation;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                if (currentProductUri == null) {
                    return new CursorLoader(this, BookStoreContract.ProductEntry.CONTENT_URI,
                            projection, null, null, null);
                } else {
                    return new CursorLoader(this, currentProductUri, projection,
                            null, null, null);
                }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            if (currentProductUri == null) {
                return;
            }

            int nameColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.PRODUCT_NAME_COLUMN);
            int priceColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.PRICE_COLUMN);
            int quantityColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.QUANTITY_COLUMN);
            int typeColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.PRODUCT_TYPE_COLUMN);
            int supplierNameColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.SUPPLIER_NAME_COLUMN);
            int supplierPhoneColumnIndex = data.getColumnIndex(BookStoreContract.ProductEntry.SUPPLIER_PHONE_NUMBER_COLUMN);

            String name = data.getString(nameColumnIndex);
            int price = data.getInt(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int type = data.getInt(typeColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierPhone = data.getString(supplierPhoneColumnIndex);

            nameText.setText(name);
            priceText.setText(String.valueOf(price));
            quantityText.setText(String.valueOf(quantity));
            supplierNameText.setText(supplierName);
            supplierPhoneText.setText(supplierPhone);

            switch (type) {
                case BookStoreContract.ProductEntry.PRODUCT_TYPE_BOOK:
                    typeSpinner.setSelection(1);
                    break;
                case BookStoreContract.ProductEntry.PRODUCT_TYPE_MAGAZINE:
                    typeSpinner.setSelection(2);
                    break;
                default:
                    typeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
