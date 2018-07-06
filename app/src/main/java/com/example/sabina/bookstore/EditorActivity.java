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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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

    private String loadedName;
    private int loadedPrice;
    private int loadedQuantity;
    private int loadedType;
    private String loadedSupplierName;
    private String loadedSupplierPhone;

    private Uri currentProductUri;
    private boolean productHasChanged = false;
    private boolean productLoaded = false;
    private int productType = ProductEntry.PRODUCT_TYPE_UNKNOWN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentProductUri = getIntent().getData();

        setupMode();
        initViews();
        setupSpinner();
        setupButtons();
    }

    private void setupButtons() {
        Button decreaseQuantityButton = (Button) findViewById(R.id.quantity_minus);
        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = getQuantityFromText();
                if (quantity > 0) {
                    quantityText.setText(String.valueOf(quantity - 1));
                }
            }
        });

        Button increaseQuantityButton = (Button) findViewById(R.id.quantity_plus);
        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = getQuantityFromText();
                quantityText.setText(String.valueOf(quantity + 1));
            }
        });

        Button orderButton = (Button) findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = supplierPhoneText.getText().toString();
                if (!phone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            }
        });

        Button deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentProductUri != null) {
                    showDeleteConfirmationDialog();
                } else {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_delete_new_product),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
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
            productLoaded = true;
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

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (productLoaded) {
                    decideChanged();
                }
            }
        };
        AdapterView.OnItemSelectedListener spinnerChangeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (productLoaded) {
                    decideChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        };

        nameText.addTextChangedListener(textWatcher);
        priceText.addTextChangedListener(textWatcher);
        quantityText.addTextChangedListener(textWatcher);
        typeSpinner.setOnItemSelectedListener(spinnerChangeListener);
        supplierNameText.addTextChangedListener(textWatcher);
        supplierPhoneText.addTextChangedListener(textWatcher);
    }

    private void decideChanged() {
        boolean nameChanged = !nameText.getText().toString().equals(loadedName);
        String priceString = priceText.getText().toString();
        if (priceString.isEmpty()) {
            priceString = "0";
        }
        boolean priceChanged = Integer.parseInt(priceString) != loadedPrice;
        String quantityString = quantityText.getText().toString();
        if (quantityString.isEmpty()) {
            quantityString = "0";
        }
        boolean quantityChanged = Integer.parseInt(quantityString) != loadedQuantity;
        boolean typeChanged = typeSpinner.getSelectedItemPosition() != loadedType;
        boolean supplierNameChanged = !supplierNameText.getText().toString().equals(loadedSupplierName);
        boolean supplierPhoneChanged = !supplierPhoneText.getText().toString().equals(loadedSupplierPhone);

        productHasChanged = nameChanged || priceChanged || quantityChanged || typeChanged || supplierNameChanged
                || supplierPhoneChanged;
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
        int price = getPriceFromText();
        int quantity = getQuantityFromText();
        String supplierName = supplierNameText.getText().toString().trim();
        String supplierPhone = supplierPhoneText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(supplierName)
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

    private int getQuantityFromText() {
        int quantity = 0;
        String quantityString = quantityText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        return quantity;
    }

    private int getPriceFromText() {
        int price = 0;
        String priceString = priceText.getText().toString().trim();
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        return price;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

            loadedName = data.getString(nameColumnIndex);
            loadedPrice = data.getInt(priceColumnIndex);
            loadedQuantity = data.getInt(quantityColumnIndex);
            loadedType = data.getInt(typeColumnIndex);
            loadedSupplierName = data.getString(supplierNameColumnIndex);
            loadedSupplierPhone = data.getString(supplierPhoneColumnIndex);

            nameText.setText(loadedName);
            priceText.setText(String.valueOf(loadedPrice));
            quantityText.setText(String.valueOf(loadedQuantity));
            supplierNameText.setText(loadedSupplierName);
            supplierPhoneText.setText(loadedSupplierPhone);

            switch (loadedType) {
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

            productLoaded = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productLoaded = false;

        nameText.setText("");
        priceText.setText("");
        quantityText.setText("");
        typeSpinner.setSelection(0);
        supplierNameText.setText("");
        supplierPhoneText.setText("");
    }
}
