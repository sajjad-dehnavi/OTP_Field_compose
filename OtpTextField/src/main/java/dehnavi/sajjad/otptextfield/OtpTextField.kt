package dehnavi.sajjad.otptextfield

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    modifierField: Modifier = Modifier,
    numField: Int = 4,
    value: String = "",
    passwordChar: Char = '•',
    isPasswordMode: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    textSizePassword: TextUnit = 36.sp,
    borderShape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = Color.Black,
    borderFocusColor: Color = Color.Blue,
    borderWrongColor: Color = Color.Red,
    borderWidth: Dp = 1.5.dp,
    borderFocusWidth: Dp = 4.dp,
    isWrong: Boolean = false,
    paddingFieldValue: Dp = 12.dp,
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(paddingFieldValue)
    ) {
        items(numField) { index ->
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

            ItemOtpField(
                modifier = modifierField,
                numField = numField,
                index = index,
                isWrong = isWrongState,
                borderShape = borderShape,
                textOtp = textOtp,
                localFocusManager = localFocusManager,
                widthBorder = widthBorderFocus.value,
                colorBorder = colorBorderFocus.value,
                passwordChar = passwordChar,
                isPasswordMode = isPasswordMode,
                disableWrongMode = { isWrongState = false },
                textStyle = textStyle,
                textSizePassword = textSizePassword,
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
    modifier: Modifier,
    numField: Int,
    index: Int,
    isWrong: Boolean,
    borderShape: Shape,
    textOtp: SnapshotStateList<String>,
    localFocusManager: FocusManager,
    textStyle: TextStyle,
    textSizePassword: TextUnit,
    widthBorder: Dp,
    colorBorder: Color,
    passwordChar: Char,
    isPasswordMode: Boolean,
    disableWrongMode: () -> Unit,
    onFocusChange: (Int) -> Unit,
    updateTextValue: (String) -> Unit,
    onFinishedChange: ((String) -> Unit)?,
) {
    val isLastField = numField - 1 == index
    val isFirstField = index == 0

    val textFieldValueState =
        TextFieldValue(
            text = if (isPasswordMode) passwordChar.toString() else textOtp[index],
            selection = TextRange(textOtp[index].length)
        )

    BasicTextField(modifier = modifier
        .border(
            width = widthBorder, color = colorBorder, shape = borderShape
        )
        .onFocusChanged {
            if (it.isFocused && it.hasFocus) {
                //set focus
                onFocusChange.invoke(index)
            }
        }
        .onKeyEvent { event: KeyEvent ->
            handleBackspaceKeyEvent(
                text = textFieldValueState.text,
                event = event,
                isFirstField = isFirstField,
                localFocusManager = localFocusManager
            )
        },
        value = textFieldValueState,
        onValueChange = {
            handleValueChange(
                oldValue = textOtp[index],
                newValue = it.text,
                numField = numField,
                textOtp = textOtp,
                localFocusManager = localFocusManager,
                isLastField = isLastField,
                isWrong = isWrong,
                disableWrongMode = disableWrongMode
            ) { updatedValue ->
                updateTextValue.invoke(updatedValue)
            }

            //all input filled
            if (checkIsAllInputFilled(textOtp)) {
                onFinishedChange?.let { listener -> listener(textOtp.joinToString("")) }
            }
        },
        textStyle = textStyle.copy(
            textAlign = TextAlign.Center,
            fontSize = if (isPasswordMode) textSizePassword else textStyle.fontSize
        ),
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
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
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
    textOtp: SnapshotStateList<String>,
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
    val pasteMode = newValue.length == (numField + oldValue.length)
    if (pasteMode) {
        val pastedValue = newValue.subSequence(oldValue.length, newValue.length)
        pastedValue.forEachIndexed { charIndex, char ->
            if (charIndex < numField) {
                textOtp[charIndex] = char.toString()

                if (charIndex != numField - 1)
                    localFocusManager.moveFocus(FocusDirection.Next)
            }
        }
    } else if (oldValue != newValue) {
        if (newValue.isEmpty()) {
            updateText.invoke(newValue)
        } else if (newValue.length == 1) {
            updateText.invoke(newValue)
            if (!isLastField) {
                localFocusManager.moveFocus(FocusDirection.Next)
            }
        } else {
            updateText.invoke(newValue.last().toString())

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
