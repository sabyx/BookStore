package com.example.sabina.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.*;
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
import android.widget.*;
import com.example.sabina.bookstore.data.BookStoreContract.ProductEntry;

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

    private boolean productHasChanged = false;
    private int productType = ProductEntry.PRODUCT_TYPE_UNKNOWN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();
        adapter = new BookCursorAdapter(this, null);

        setupMode();
        initViews();
        setupSpinner();
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
                        productType = ProductEntry.PRODUCT_TYPE_BOOK;
                    } else if (selection.equals(getString(R.string.product_magazine))) {
                        productType = ProductEntry.PRODUCT_TYPE_MAGAZINE;
                    } else {
                        productType = ProductEntry.PRODUCT_TYPE_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productType = ProductEntry.PRODUCT_TYPE_UNKNOWN;
            }
        });
    }

    private void setupMode() {
        if (currentProductUri == null) {
            setTitle(R.string.editor_view_add_product);
        } else {
            setTitle(R.string.editor_view_edit_product);
            getLoaderManager().initLoader(URI_LOADER, null, this);
        }
    }

    private void initViews() {
        nameText = (EditText) findViewById(R.id.edit_product_name);
        priceText = (EditText) findViewById(R.id.edit_product_price);
        quantityText = (EditText) findViewById(R.id.edit_product_quantity);
        typeSpinner = (Spinner) findViewById(R.id.spinner_type);
        supplierNameText = (EditText) findViewById(R.id.edit_supplier_name);
        supplierPhoneText = (EditText) findViewById(R.id.edit_supplier_phone);

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
                } else {
                    showDiscardDialog();
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

        if (currentProductUri == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(supplierName)
                || price <= 0 || quantity <= 0 || TextUtils.isEmpty(supplierPhone)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.PRODUCT_NAME_COLUMN, name);
        values.put(ProductEntry.PRODUCT_PRICE_COLUMN, price);
        values.put(ProductEntry.PRODUCT_QUANTITY_COLUMN, quantity);
        values.put(ProductEntry.PRODUCT_TYPE_COLUMN, productType);
        values.put(ProductEntry.PRODUCT_SUPPLIER_NAME_COLUMN, supplierName);
        values.put(ProductEntry.PRODUCT_SUPPLIER_PHONE_NUMBER_COLUMN, supplierPhone);

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        switch (id) {
//            case URI_LOADER:
//                if (currentProductUri == null) {
//                    return new CursorLoader(this, ProductEntry.CONTENT_URI,
//                            ProductEntry.EDITOR_PROJECTION, null, null, null);
//                } else {
//                    return new CursorLoader(this, currentProductUri, ProductEntry.EDITOR_PROJECTION,
//                            null, null, null);
//                }
//        }
//        return null;

        return new CursorLoader(this, currentProductUri, ProductEntry.EDITOR_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_NAME_COLUMN);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_PRICE_COLUMN);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_QUANTITY_COLUMN);
            int typeColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_TYPE_COLUMN);
            int supplierNameColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_SUPPLIER_NAME_COLUMN);
            int supplierPhoneColumnIndex = data.getColumnIndex(ProductEntry.PRODUCT_SUPPLIER_PHONE_NUMBER_COLUMN);

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
                case ProductEntry.PRODUCT_TYPE_BOOK:
                    typeSpinner.setSelection(1);
                    break;
                case ProductEntry.PRODUCT_TYPE_MAGAZINE:
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
        nameText.setText("");
        priceText.setText("");
        quantityText.setText("");
        typeSpinner.setSelection(0);
        supplierNameText.setText("");
        supplierPhoneText.setText("");
    }
}
