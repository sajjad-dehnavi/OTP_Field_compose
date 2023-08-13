package dehnavi.sajjad.otptextfield

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OtpTextField(
    numField: Int = 4,
    borderShape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = Color.White,
    borderFocusColor: Color = Color.Blue,
    borderWidth: Dp = 1.dp,
    borderFocusWidth: Dp = 4.dp,
) {
    val localFocusManager = LocalFocusManager.current

    val textOtp = remember {
        mutableStateListOf<String>().apply {
            repeat(numField) {
                add("")
            }
        }
    }

    var focusIndex by remember {
        mutableStateOf(0)
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(numField) { index ->
            val isLastField = numField - 1 == index
            val isFirstField = index == 0

            val colorBorderFocus = animateColorAsState(
                targetValue = if (index == focusIndex) borderFocusColor else borderColor,
                label = "COLOR"
            )
            val widthBorderFocus = animateDpAsState(
                targetValue = if (index == focusIndex) borderFocusWidth else borderWidth,
                label = "WIDTH"
            )

            BasicTextField(
                modifier = Modifier
                    .border(
                        width = widthBorderFocus.value,
                        color = colorBorderFocus.value,
                        shape = borderShape
                    )
                    .width(40.dp)
                    .height(60.dp)
                    .onFocusChanged { if (it.isFocused) focusIndex = index }
                    .onKeyEvent { event: KeyEvent ->
                        handleBackspaceKeyEvent(
                            event,
                            index,
                            textOtp,
                            isFirstField,
                            localFocusManager
                        )
                    },
                value = textOtp[index],
                onValueChange = {
                    handleValueChange(
                        it,
                        index,
                        numField,
                        textOtp,
                        localFocusManager,
                        isLastField
                    )
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
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleBackspaceKeyEvent(
    event: KeyEvent,
    index: Int,
    textOtp: MutableList<String>,
    isFirstField: Boolean,
    localFocusManager: FocusManager,
): Boolean {
    if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && textOtp[index].isEmpty()) {
        if (!isFirstField) {
            localFocusManager.moveFocus(FocusDirection.Previous)
        }
        return true
    }
    return false
}

private fun handleValueChange(
    newValue: String,
    index: Int,
    numField: Int,
    textOtp: MutableList<String>,
    localFocusManager: FocusManager,
    isLastField: Boolean,
) {
    val pasteMode = (newValue.length - textOtp[index].length) == numField
    if (pasteMode) {
        newValue.forEachIndexed { charIndex, char ->
            if (charIndex < numField) {
                textOtp[charIndex] = char.toString()

                if (charIndex < numField - 1) {
                    localFocusManager.moveFocus(FocusDirection.Next)
                }
            }
        }
    } else {
        if (newValue.isEmpty()) {
            textOtp[index] = newValue
        } else if (newValue.length == 1) {
            textOtp[index] = newValue
            if (!isLastField) {
                localFocusManager.moveFocus(FocusDirection.Next)
            }
        } else {
            val lastChar = newValue.last().toString()
            val firstChar = newValue.first().toString()

            textOtp[index] = if (textOtp[index] == firstChar) lastChar else firstChar

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
