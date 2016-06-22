package io.hypertrack.meta.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by piyush on 22/06/16.
 */
public class KeyboardUtils {

    /**
     * Method to show Keyboard implicitly with @param editText as the focus
     * @param context
     * @param editText
     */
    public static void showKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
