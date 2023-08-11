package dehnavi.sajjad.otptextfield

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    borderWidth: Dp = 1.dp,
) {
    val localFocusManager = LocalFocusManager.current
    val textOtp = remember {
        mutableStateListOf<String>().apply {
            repeat(numField) {
                add("")
            }
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(numField) { index ->
            val isLastField = numField - 1 == index
            val isFirstField = index == 0

            BasicTextField(
                modifier = Modifier
                    .border(width = borderWidth, color = borderColor, shape = borderShape)
                    .width(40.dp)
                    .height(60.dp),
                value = textOtp[index],

                onValueChange = {
                    val pasteMode = (it.length - textOtp[index].length) == numField
                    if (pasteMode) {
                        it.forEachIndexed { index, char ->
                            if (index < numField) {
                                textOtp[index] = char.toString()
                            }
                        }
                    } else {

                        if (it.isEmpty()) {
                            textOtp[index] = it
                            if (!isFirstField)
                                localFocusManager.moveFocus(
                                    FocusDirection.Previous
                                )
                        } else if (it.length == 1) {
                            textOtp[index] = it
                            if (!isLastField)
                                localFocusManager.moveFocus(
                                    FocusDirection.Next
                                )
                        } else {
                            val lastChar = it.last().toString()
                            val firstChar = it.first().toString()

                            textOtp[index] =
                                if (textOtp[index] == firstChar) lastChar else firstChar

                            if (!isLastField)
                                localFocusManager.moveFocus(
                                    FocusDirection.Next
                                )
                        }
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
                }
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    OtpTextField()
}
