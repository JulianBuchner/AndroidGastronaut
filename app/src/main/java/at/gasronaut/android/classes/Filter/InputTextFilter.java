package at.gasronaut.android.classes.Filter;

import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by p.rathgeb on 15.08.2017.
 */

/**
 * Filter for accepting only valid indices or prefixes of the string
 * representation of valid indices.
 */
public class InputTextFilter extends NumberKeyListener {
    private NumberPicker numberPicker;
    private EditText editText;

    /**
     * The numbers accepted by the input text's {@link android.view.LayoutInflater.Filter}
     */
    private final char[] DIGIT_CHARACTERS = new char[] {
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668'
            , '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8'
            , '\u06f9',
            // Hindi and Marathi (Devanagari script)
            '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e'
            , '\u096f',
            // Bengali
            '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee'
            , '\u09ef',
            // Kannada
            '\u0ce6', '\u0ce7', '\u0ce8', '\u0ce9', '\u0cea', '\u0ceb', '\u0cec', '\u0ced', '\u0cee'
            , '\u0cef'
    };

    public InputTextFilter(NumberPicker numPicker, EditText edText) {
        super();

        this.numberPicker = numPicker;
        this.editText = edText;
    }

    // XXX This doesn't allow for range limits when controlled by a
    // soft input method!
    public int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }

    @Override
    protected char[] getAcceptedChars() {
        return DIGIT_CHARACTERS;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            Log.v("filter", "source:" + source.toString());
            String result = "";

            CharSequence filtered = String.valueOf(source.subSequence(start, end));

            Log.v("filter", "filtered:" + filtered.toString());
            if (TextUtils.isEmpty(filtered)) {
                return "";
            }
            result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                    + dest.subSequence(dend, dest.length());

            final String str = String.valueOf(result).toLowerCase();

            if (str.length() > 0) {
                try {
                    int value = Integer.parseInt(str);

                    if (numberPicker.getMinValue() <= value && value <= numberPicker.getMaxValue()) {
                        for (String val : numberPicker.getDisplayedValues()) {
                            String valLowerCase = val.toLowerCase();
                            if (valLowerCase.startsWith(str)) {
                                final int selstart = result.length();
                                final int selend = val.length();
                                numberPicker.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            editText.setSelection(selstart, selend);
                                        } catch (Exception e){}
                                    }
                                });
                                return val.subSequence(dstart, val.length());
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    //continue with the checking
                }

                for (String val : numberPicker.getDisplayedValues()) {
                    String valLowerCase = val.toLowerCase();
                    if (valLowerCase.startsWith(str)) {
                        final int selstart = result.length();
                        final int selend = val.length();
                        numberPicker.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    editText.setSelection(selstart, selend);
                                } catch (Exception e){}
                            }
                        });
                        return val.subSequence(dstart, val.length());
                    }
                }

                editText.setText(dest);
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
