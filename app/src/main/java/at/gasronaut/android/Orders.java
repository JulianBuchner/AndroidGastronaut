package at.gasronaut.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.gastronaut.R;
import com.android.gastronaut.classes.Filter.InputTextFilter;
import com.android.gastronaut.classes.Filter.MinMaxFilter;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.gasronaut.android.classes.Configuration;
import at.gasronaut.android.classes.MenuItem;
import at.gasronaut.android.classes.Order;
import at.gasronaut.android.classes.RenamedPosition;
import at.gasronaut.android.classes.TableConfig;
import at.gasronaut.android.classes.Voucher;

public class Orders extends AbstractActivity {
    Order order = new Order();
    private static String additionalInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        order.additionalInfo = additionalInfo;
        Configuration configuration = Configuration.getInstance();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.orders);

        //HashMap<Integer, Integer> orders = configuration.orderList;

        ArrayList<Integer> sorted = Order.sortEntries(configuration.orderList);

        for (Integer key : sorted) {
            addNewItem(key);
        }

        hideKeyboard();

        resyncList();
    }

    private void removeItem(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText mEdit = (EditText) findViewById(id);

                if (mEdit != null) {
                    TableRow r = (TableRow) mEdit.getParent();
                    r.removeAllViews();
                }
            }
        });
    }

    private void updateItem(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText mEdit = (EditText) findViewById(id);

                if (mEdit != null) {
                    mEdit.setText(String.valueOf(Configuration.orderList.get(id)));
                }
            }
        });
    }

    private void addNewItem(int key) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = ((size.x - 60) / 6);

        TableLayout layout = findViewById(R.id.OrdersTableLayout);

        final Integer value = Configuration.orderList.get(key);

        TableRow row = new TableRow(this);

        TextView text = new TextView(this);
        EditText edit = new EditText(this);

        TableRow.LayoutParams tr = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tr.setMargins(0, 0, 0, 5);
        tr.weight = 0;

        TableRow.LayoutParams fields = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        fields.weight = 0;

        edit.setLayoutParams(fields);
        edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        edit.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        //edit.setWidth(width);

        edit.setWidth(width);

        edit.setSelectAllOnFocus(true);
        edit.setId(key);
        edit.setPadding(0, 0, 30, 10);

        Button btn = new Button(this);

        btn.setText("x");
        btn.setId(key);
        //btn.setPadding(0, 0, 0, 0);
        btn.setWidth(width);
        btn.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_OVER);
        //btn.setBackgroundColor(Color.RED);

        //btn.setShadowLayer(1, 1, 1, Color.BLACK);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();

                Configuration.getInstance().orderList.remove(id);
                TableRow r = (TableRow) v.getParent();
                r.removeAllViews();

                ImageButton btn = (ImageButton) findViewById(R.id.sendOrderButton);
                btn.setEnabled(!Configuration.getInstance().orderList.isEmpty());

                Configuration.getInstance().itemTimestamp = Configuration.getInstance().orderList.isEmpty() ? 0 : Configuration.getInstance().itemTimestamp;
            }
        });

        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resyncList();
                if (hasFocus) {
                    ((EditText) v).selectAll();

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(v, 0);
                }
            }
        });

        edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);   // handle the event first
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {

                    imm.showSoftInput(v, 0);  // hide the soft keyboard
                    //edit.setCursorVisible(true); //This is to display cursor when upon onTouch of Edittext
                }
                return true;
            }
        });

        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(((EditText) v).getText().toString());
                    } catch (Exception e) {
                    }

                    int id = v.getId();

                    if (amount <= 0) {
                        TableRow r = (TableRow) v.getParent();
                        r.removeAllViews();
                        Configuration.getInstance().orderList.remove(id);
                    } else {
                        if (Configuration.getInstance().orderList.get(id) > 0) {
                            Configuration.getInstance().orderList.remove(id);
                            Configuration.getInstance().orderList.put(id, amount);
                        }
                    }

                    ImageButton btn = (ImageButton) findViewById(R.id.sendOrderButton);
                    btn.setEnabled(!Configuration.getInstance().orderList.isEmpty());

                    Configuration.getInstance().itemTimestamp = Configuration.getInstance().orderList.isEmpty() ? 0 : Configuration.getInstance().itemTimestamp;
                }
                return false;
            }
        });

        text.setLayoutParams(tr);
        text.setWidth(4 * width);
        text.setTextSize(21);

        final MenuItem item = Configuration.getItem(key);
        text.setText(item.toString());
        edit.setText(String.format("%d", value));

        if (!(item instanceof RenamedPosition)) {
            text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Position editieren");

                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    llp.setMargins(15, 0, 0, 0);

                    LinearLayout layout = new LinearLayout(getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);

                    /*TextView posText = new TextView(AbstractActivity.getActivity());
                    posText.setText(item.name);
                    posText.setLayoutParams(llp);
                    layout.addView(posText);*/

                    final EditText posName = new EditText(getActivity());

                    posName.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String prefix = item.name + " ";
                            if (!s.toString().startsWith(prefix)) {
                                String cleanString = "";
                                String deletedPrefix = prefix.substring(0, prefix.length() - 1);
                                if (s.toString().startsWith(deletedPrefix)) {
                                    cleanString = s.toString().replaceAll(deletedPrefix, "");
                                }
                                posName.setText(prefix + cleanString);
                                posName.setSelection(prefix.length());
                            }
                        }
                    });

                    //posName.mPrefix = item.name;
                    //posName.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    //posName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    posName.setMaxLines(1);
                    posName.setHorizontalScrollBarEnabled(true);
                    posName.setSingleLine(true);
                    posName.setMovementMethod(new ScrollingMovementMethod());
                    //posName.setText(item.name);
                    posName.setLayoutParams(llp);
                    posName.requestFocus();
                    //posName.setHint("zusätzliche Info");
                    layout.addView(posName);

                    final EditText amount = new EditText(getActivity());
                    amount.setLayoutParams(llp);
                    amount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    amount.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
                    amount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                    amount.setFilters(new InputFilter[]{new MinMaxFilter("1", String.valueOf(Configuration.orderList.get(item.id)))});
                    amount.setHint("Menge (max. " + Configuration.orderList.get(item.id) + ")");
                    amount.setSelectAllOnFocus(true);
                    layout.addView(amount);

                    alertDialogBuilder.setView(layout);

                    //alertDialogBuilder.setView(linearLayout);
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Übernehmen",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            int selAmount = 0;
                                            try {
                                                selAmount = Integer.parseInt(amount.getText().toString());
                                            } catch (Exception e) {
                                            }

                                            selAmount = selAmount <= 0 ? 1 : selAmount;

                                            String newName = posName.getText().toString().substring((item.name + " ").length());

                                            if (selAmount > 0 && newName.trim().length() > 0) {
                                                int tmpId = Configuration.getTempMenuItemId();

                                                RenamedPosition pos = new RenamedPosition(tmpId, newName, item.clone());
                                                pos.version = Configuration.generateVersionForItem(item.id);

                                                Configuration.tempItems.put(tmpId, pos);

                                                Configuration.orderList.put(tmpId, selAmount);

                                                addNewItemUiThread(tmpId);

                                                selAmount = Configuration.orderList.get(item.id) - selAmount;
                                                Configuration.orderList.remove(item.id);

                                                if (selAmount > 0) {
                                                    Configuration.orderList.put(item.id, selAmount);
                                                    updateItem(item.id);
                                                } else {
                                                    removeItem(item.id);
                                                }
                                            }

                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("Abbrechen",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.dismiss();
                                        }
                                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    alertDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                    alertDialog.show();

                    return true;
                }
            });
        }

        row.addView(text);
        row.addView(edit);
        row.addView(btn);
        layout.addView(row);
    }

    private void addNewItemUiThread(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addNewItem(id);
            }
        });
    }

    private void resyncList() {
        List<Integer> remove = new ArrayList<>();
        Configuration configuration = Configuration.getInstance();
        for (Integer key : configuration.orderList.keySet()) {
            EditText mEdit = (EditText) findViewById(key);

            if (mEdit != null) {
                int amount = 0;
                try {
                    amount = Integer.parseInt(mEdit.getText().toString());
                } catch (Exception e) {
                }

                if (amount <= 0) {
                    remove.add(key);

                    TableRow r = (TableRow) mEdit.getParent();
                    r.removeAllViews();
                } else {
                    if (configuration.orderList.get(key) > 0) {
                        configuration.orderList.put(key, amount);
                    }
                }
            }
        }

        for (int idx : remove) {
            configuration.orderList.remove(idx);
        }

        ImageButton btn = (ImageButton) findViewById(R.id.sendOrderButton);
        btn.setEnabled(!configuration.orderList.isEmpty());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            resyncList();
        }

        super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 2
        if (requestCode == REQUEST_CODE_ADDITIONAL_INFO && resultCode == RESULT_CODE_OK) {
            order.additionalInfo = data.getStringExtra("additionalInfo");
            additionalInfo = order.additionalInfo;
        } else if (requestCode == REQUEST_CODE_SCAN_TABLE) {
            if (resultCode == RESULT_OK) {

                TableConfig.selectedTableId = Integer.parseInt(data.getStringExtra("SCAN_RESULT").split(";")[1]);
                TableConfig.dialogOpen = false;

                sendOrder();
            }
        }
    }

    public void additionalInfoButtonClick(View v) {
        Intent intent = new Intent(this, PaintActivity.class);
        intent.putExtra("additionalInfo", additionalInfo);

        startActivityForResult(intent, REQUEST_CODE_ADDITIONAL_INFO);
    }


    public void sendOrderButtonClick(View v) {
        resyncList();

        ContextThemeWrapper cw = new ContextThemeWrapper(this, R.style.NumberPickerText);
        RelativeLayout linearLayout = new RelativeLayout(this);
        final NumberPicker aNumberPicker = new NumberPicker(cw);

        String array[] = new String[TableConfig.getInstance().getNumTables()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = TableConfig.getInstance().getTableName(i + 1);
        }

        aNumberPicker.setMinValue(1);
        aNumberPicker.setMaxValue(array.length);

        aNumberPicker.setWrapSelectorWheel(true);
        aNumberPicker.setDisplayedValues(array);

        aNumberPicker.setValue(TableConfig.lastTableId);

        if (TableConfig.getInstance().isNumeric()) {
            try {
                ((EditText) aNumberPicker.getChildAt(0)).setRawInputType(InputType.TYPE_CLASS_NUMBER);
            } catch (Exception e) {
                ((EditText) aNumberPicker.getChildAt(1)).setRawInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else {
            try {
                ((EditText) aNumberPicker.getChildAt(0)).setRawInputType(InputType.TYPE_CLASS_TEXT);
            } catch (Exception e) {
                ((EditText) aNumberPicker.getChildAt(1)).setRawInputType(InputType.TYPE_CLASS_TEXT);
            }

            EditText input = findInput(aNumberPicker);
            input.setFilters(new InputFilter[]{new InputTextFilter(aNumberPicker, input)});
        }

        aNumberPicker.clearFocus();


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker, numPickerParams);

        order.phoneId = android.provider.Settings.Secure.getString(Configuration.getInstance().activity.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        String neutralButton = "Tisch scannen";
        /*if (Configuration.getInstance().selectTableType.equals(Configuration.SELECT_TABLE_TYPE_SCAN)) {
            neutralButton = "Tisch eingeben";
        }*/

        if (TableConfig.showTableForEntries() && TableConfig.showTableDialog == TableConfig.SELECT_TABLE_END) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Tischnummer eingeben:");
            alertDialogBuilder.setView(linearLayout);
            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                aNumberPicker.clearFocus();
                                TableConfig.selectedTableId = aNumberPicker.getValue();
                                TableConfig.dialogOpen = false;

                                sendOrder();
                            }
                        })
                .setNegativeButton("Abbrechen",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                TableConfig.dialogOpen = false;

                                dialog.cancel();
                            }
                        }).
                setNeutralButton(neutralButton, (dialogInterface, i) -> {
                    Intent intent = new Intent(Configuration.getInstance().activity, CaptureActivity.class);
                    intent.setAction("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SAVE_HISTORY", false);
                    Configuration.getInstance().activity.startActivityForResult(intent, REQUEST_CODE_SCAN_TABLE);
                });
            AlertDialog alertDialog = alertDialogBuilder.create();
            TableConfig.dialogOpen = true;

            alertDialog.show();

        } else if (TableConfig.showTableForEntries() && TableConfig.showTableDialog == TableConfig.SELECT_TABLE_FIRST) {
            if (!TableConfig.tableSelected) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Tischnummer eingeben:");
                alertDialogBuilder.setView(linearLayout);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        aNumberPicker.clearFocus();
                                        TableConfig.selectedTableId = aNumberPicker.getValue();
                                        TableConfig.dialogOpen = false;

                                        sendOrder();
                                    }
                                })
                        .setNegativeButton("Abbrechen",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        TableConfig.dialogOpen = false;

                                        dialog.cancel();
                                    }
                                }).
                        setNeutralButton(neutralButton, (dialogInterface, i) -> {
                            Intent intent = new Intent(Configuration.getInstance().activity, CaptureActivity.class);
                            intent.setAction("com.google.zxing.client.android.SCAN");
                            intent.putExtra("SAVE_HISTORY", false);
                            Configuration.getInstance().activity.startActivityForResult(intent, REQUEST_CODE_SCAN_TABLE);
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                TableConfig.dialogOpen = true;
            } else {
                sendOrder();
            }
        } else {
            TableConfig.selectedTableId = 1;

            sendOrder();
        }
    }

    @Override
    public void orderDone(int orderId, final int timestamp, final ArrayList<Voucher> vouchers) {
        additionalInfo = "";
        final Activity reference = this;
        final int finalOrderId = orderId;

        RelativeLayout linearLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        linearLayout.setLayoutParams(params);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bestellung erfolgreich abgeschlossen!");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.dismiss();
                                reference.finish();

                                Boolean showBill = false;
                                for (Integer key : order.items.keySet()) {
                                    showBill = Configuration.getItem(key).showBillDialog && !Configuration.getItem(key).hideOnBill;
                                    if (showBill) {
                                        break;
                                    }
                                }

                                if (showBill) {
                                    Configuration.tableVouchers = null;
                                    Configuration.tableVouchers = vouchers;

                                    Order newOrder = order.clone();
                                    newOrder.additionalInfo = "";
                                    order.items = (HashMap<Integer, Integer>) Configuration.getInstance().orderList.clone();
                                    order.orderId = finalOrderId;
                                    order.timestamp = timestamp;

                                    Intent intent = new Intent(reference, Bill.class);
                                    intent.putExtra("order", order);
                                    startActivity(intent);
                                }

                                Configuration.orderList.clear();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void orderError() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Fehler");

        TextView text = new TextView(this);
        text.setText("Es ist ein Fehler beim Abschluss der Bestellung aufgetreten.\n\nBitte prüfen Sie ihre Verbindung (WLAN / mobile Daten) und versuche Sie es erneut!");
        alertDialogBuilder.setView(text);

        //alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void ButtonClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                resyncList();

                this.finish();
                break;

            case R.id.lastorderButton:
                resyncList();

                this.finish();

                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);

                break;
        }
    }

    public void sendOrder() {
        order.tableId = TableConfig.selectedTableId;
        order.items = Configuration.orderList;

        File file = new File(getContext().getFilesDir(), "AdditionalInfo.png");
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            order.base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        TableConfig.tableSelected = false;
        Order.sendOrder(order);
    }
}
