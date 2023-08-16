package dehnavi.sajjad.otptextfield

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OtpTextField(
    numField: Int = 4,
    value: String = "",
    borderShape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = Color.White,
    borderFocusColor: Color = Color.Blue,
    borderWrongColor: Color = Color.Red,
    borderWidth: Dp = 1.dp,
    borderFocusWidth: Dp = 4.dp,
    isWrong: Boolean = true,
    onFinishedChange: ((String) -> Unit)? = null,
) {
    val localFocusManager = LocalFocusManager.current


    var isWrongState = isWrong

    var focusIndex by remember {
        mutableStateOf(0)
    }


    val textOtp = remember {
        mutableStateListOf<String>().apply {
            if (value.isNotEmpty()) {
                repeat(numField) {
                    val char = if (it <= value.length - 1) {
                        focusIndex = if (it == value.length - 1)
                            it
                        else
                            it + 1

                        value[it].toString()
                    } else ""
                    add(char)
                }
            }
        }
    }


    //all input filled
    if (checkIsAllInputFilled(textOtp)) {
        onFinishedChange?.let { listener -> listener(textOtp.joinToString()) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(numField) { index ->
            ItemOtpField(
                numField,
                index,
                isWrongState,
                borderWrongColor,
                focusIndex,
                borderFocusColor,
                borderColor,
                borderFocusWidth,
                borderWidth,
                borderShape,
                textOtp,
                localFocusManager,
                disableWrongMode = { isWrongState = false },
                onFocusChange = { newIndex ->
                    focusIndex = newIndex
                },
                onFinishedChange = onFinishedChange,
                updateTextValue = {
                    textOtp[index] = it
                }
            )
        }
    }
}

@Composable
private fun ItemOtpField(
    numField: Int,
    index: Int,
    isWrong: Boolean,
    borderWrongColor: Color,
    focusIndex: Int,
    borderFocusColor: Color,
    borderColor: Color,
    borderFocusWidth: Dp,
    borderWidth: Dp,
    borderShape: Shape,
    textOtp: SnapshotStateList<String>,
    localFocusManager: FocusManager,
    disableWrongMode: () -> Unit,
    onFocusChange: (Int) -> Unit,
    updateTextValue: (String) -> Unit,
    onFinishedChange: ((String) -> Unit)?,
) {
    val isLastField = numField - 1 == index
    val isFirstField = index == 0

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = textOtp[index]
            )
        )
    }

    val colorBorderFocus = animateColorAsState(
        targetValue = if (isWrong) {
            borderWrongColor
        } else {
            if (index == focusIndex) borderFocusColor else borderColor
        }, label = "COLOR"
    )
    val widthBorderFocus = animateDpAsState(
        targetValue = if (index == focusIndex || isWrong) borderFocusWidth else borderWidth,
        label = "WIDTH"
    )

    BasicTextField(modifier = Modifier
        .border(
            width = widthBorderFocus.value, color = colorBorderFocus.value, shape = borderShape
        )
        .width(40.dp)
        .height(60.dp)
        .onFocusChanged {
            if (it.isFocused && it.hasFocus) {
                //set focus
                onFocusChange.invoke(index)
                //set cursor selection
                textFieldValueState =
                    textFieldValueState.copy(selection = TextRange(textFieldValueState.text.length))
            }
        }
        .onKeyEvent { event: KeyEvent ->
            handleBackspaceKeyEvent(
                textFieldValueState.text, event, isFirstField, localFocusManager
            )
        },
        value = textFieldValueState,
        onValueChange = {
            handleValueChange(
                textFieldValueState.text,
                it.text,
                numField,
                localFocusManager,
                isLastField,
                isWrong,
                disableWrongMode
            ) { updatedValue ->
                updateTextValue.invoke(updatedValue)

                textFieldValueState = textFieldValueState.copy(text = updatedValue)
            }

            //all input filled
            if (checkIsAllInputFilled(textOtp)) {
                onFinishedChange?.let { listener -> listener(textOtp.joinToString()) }
            }
        },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardActions = KeyboardActions(onNext = {
            localFocusManager.moveFocus(
                FocusDirection.Next
            )
        }),
        keyboardOptions = KeyboardOptions(
            imeAction = if (!isLastField) ImeAction.Next else ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Decimal
        ),
        decorationBox = { innerTextField ->
            Column(verticalArrangement = Arrangement.Center) {
                innerTextField()
            }
        })
}

private fun checkIsAllInputFilled(list: List<String>): Boolean {
    list.forEach { if (it.isEmpty()) return false }
    return true
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleBackspaceKeyEvent(
    text: String,
    event: KeyEvent,
    isFirstField: Boolean,
    localFocusManager: FocusManager,
): Boolean {
    if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && text.isEmpty()) {
        if (!isFirstField) {
            localFocusManager.moveFocus(FocusDirection.Previous)
        }
        return true
    }
    return false
}

private fun handleValueChange(
    oldValue: String,
    newValue: String,
    numField: Int,
    localFocusManager: FocusManager,
    isLastField: Boolean,
    isWrong: Boolean,
    disableWrongMode: () -> Unit,
    updateText: (String) -> Unit,
) {
    //disable wrong mode
    if (isWrong) {
        disableWrongMode.invoke()
    }
    //handle paste mode
    val pasteMode = (newValue.length - numField) == numField
    if (pasteMode) {
        newValue.forEachIndexed { charIndex, char ->
            if (charIndex < numField) {
                updateText.invoke(char.toString())

                if (charIndex < numField - 1) {
                    localFocusManager.moveFocus(FocusDirection.Next)
                }
            }
        }
    } else {
        if (newValue.isEmpty()) {
            updateText.invoke(newValue)
        } else if (newValue.length == 1) {
            updateText.invoke(newValue)
            if (!isLastField) {
                localFocusManager.moveFocus(FocusDirection.Next)
            }
        } else {
            val lastChar = newValue.last().toString()
            val firstChar = newValue.first().toString()

            val newChar = if (oldValue == firstChar) lastChar else firstChar


            Log.d(
                "TAG",
                "handleValueChange: new value $newValue  |||  first: $firstChar   ||| last: $lastChar  ||| new char: $newChar"
            )

            updateText.invoke(newChar)

            if (!isLastField) {
                localFocusManager.moveFocus(FocusDirection.Next)
            }
        }
    }
}


@Preview
@Composable
fun Preview() {
    OtpTextField()
}
