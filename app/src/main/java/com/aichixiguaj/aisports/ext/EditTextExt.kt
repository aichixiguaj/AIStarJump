package com.aichixiguaj.aisports.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2022/8/19 14:04
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    EditText 扩展
 */

inline fun EditText.addTextOnChangedListener(
    crossinline onTextChanged: (
        text: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) -> Unit = { _, _, _, _ -> },
): TextWatcher {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged.invoke(text, start, before, count)
        }
    }
    addTextChangedListener(textWatcher)
    return textWatcher
}