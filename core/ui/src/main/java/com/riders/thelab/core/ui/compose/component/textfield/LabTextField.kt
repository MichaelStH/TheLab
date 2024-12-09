package com.riders.thelab.core.ui.compose.component.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.keyboardAsState
import com.riders.thelab.core.ui.utils.UIManager
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


///////////////////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////////////////

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabTextField(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    query: String,
    onUpdateQuery: (String) -> Unit,
    placeholder: String,
    label: String,
    textColor: Color = Color.LightGray,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeActionsSearch: (() -> Unit)? = null,
    onImeActionsDone: (() -> Unit)? = null,
    hasUnderlineVisible: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState = keyboardAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocus by interactionSource.collectIsFocusedAsState()

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var isFieldFocused: Boolean by remember { mutableStateOf(false) }


    TheLabTheme {
        TextField(
            modifier = Modifier
                .then(modifier)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    Timber.d("Recomposition | BasicTextField2.onFocusChanged | onFocusChanged: $it")

                    if (isFieldFocused != it.isFocused) {
                        isFieldFocused = it.isFocused
                        if (!it.isFocused) {
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | hideKeyboard")
                            UIManager.hideKeyboard(context = context, view = view)
                        } else {
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | show keyboard")
                            UIManager.showKeyboard(context = context, view = view)
                        }
                    }
                }
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        scope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .indication(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ),
            value = query,
            onValueChange = onUpdateQuery,
            textStyle = TextStyle(textAlign = TextAlign.Justify, color = textColor),
            placeholder = { Text(text = placeholder, color = textColor) },
            label = { Text(text = label, color = textColor) },
            interactionSource = interactionSource,
            enabled = true,
            singleLine = true,
            leadingIcon = leadingContent,
            trailingIcon = trailingContent,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // perform some action
                    focusManager.clearFocus()
                    onImeActionsSearch?.invoke()
                },
                onDone = {
                    // perform some action
                    focusManager.clearFocus()
                    onImeActionsDone?.invoke()
                }
            ),
            visualTransformation = VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (!hasUnderlineVisible) Color.Transparent else MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = if (!hasUnderlineVisible) Color.Transparent else MaterialTheme.colorScheme.onPrimary,
                focusedSupportingTextColor = if (!hasUnderlineVisible) Color.Transparent else MaterialTheme.colorScheme.onPrimary,
                unfocusedSupportingTextColor = if (!hasUnderlineVisible) Color.Transparent else MaterialTheme.colorScheme.onPrimary
            ),
            readOnly = false
        )
    }

    LaunchedEffect(keyboardState) {
        Timber.d("LaunchedEffect | keyboardState shown : ${keyboardState.value} | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState.value) {
            Timber.d("KeyboardState | should show keyboard ?")
        } else {
            //show fab button
        }
    }

    // LaunchedEffect prevents endless focus request
    LaunchedEffect(focusRequester) {
        Timber.d("LaunchedEffect | focusRequester: $focusRequester | coroutineContext: ${this.coroutineContext}")

        if (keyboardState.equals(true)) {
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
            // UIManager.showKeyboard(context = context, view = view)
        }
    }

    LaunchedEffect(interactionSource) {
        Timber.d("LaunchedEffect | interactionSource: $interactionSource | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
        if (isFocus) {
            Timber.d("Focused")
        }
    }

    LaunchedEffect(onOutsideBoundariesClicked) {
        Timber.d("LaunchedEffect | onOutsideBoundariesClicked: $onOutsideBoundariesClicked | coroutineContext: ${this.coroutineContext}")
        if (onOutsideBoundariesClicked) {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabOutlinedTextField(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    query: String,
    onUpdateQuery: (String) -> Unit,
    placeholder: String,
    label: String,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    focusedBorderColor: Color = Color.Unspecified,
    unfocusedBorderColor: Color = Color.Unspecified,
    focusedContainerColor: Color = Color.Transparent,
    unfocusedContainerColor: Color = Color.Transparent
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState = keyboardAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocus by interactionSource.collectIsFocusedAsState()

    var isFieldFocused: Boolean by remember { mutableStateOf(false) }


    TheLabTheme {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    Timber.d("Recomposition | BasicTextField2.onFocusChanged | onFocusChanged: $it")

                    if (isFieldFocused != it.isFocused) {
                        isFieldFocused = it.isFocused
                        if (!it.isFocused) {
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | hideKeyboard")
                            UIManager.hideKeyboard(context = context, view = view)
                        } else {
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | show keyboard")
                            UIManager.showKeyboard(context = context, view = view)
                        }
                    }
                }
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        scope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .indication(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                )
                .then(modifier),
            value = query,
            onValueChange = onUpdateQuery,
            textStyle = TextStyle(textAlign = TextAlign.Justify, color = Color.LightGray),
            placeholder = { Text(text = placeholder, color = Color.LightGray) },
            label = { Text(text = label, color = Color.LightGray) },
            interactionSource = interactionSource,
            enabled = true,
            singleLine = true,
            leadingIcon = leadingContent,
            trailingIcon = trailingContent,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text
            ),
            visualTransformation = VisualTransformation.None,
            readOnly = false,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = focusedContainerColor,
                unfocusedContainerColor = unfocusedContainerColor,
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor
            )
        )
    }

    LaunchedEffect(keyboardState) {
        Timber.d("LaunchedEffect | keyboardState shown : ${keyboardState.value} | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState.value) {
            Timber.d("KeyboardState | should show keyboard ?")
        } else {
            //show fab button
        }
    }

    // LaunchedEffect prevents endless focus request
    LaunchedEffect(focusRequester) {
        Timber.d("LaunchedEffect | focusRequester: $focusRequester | coroutineContext: ${this.coroutineContext}")

        if (keyboardState.equals(true)) {
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
            // UIManager.showKeyboard(context = context, view = view)
        }
    }

    LaunchedEffect(interactionSource) {
        Timber.d("LaunchedEffect | interactionSource: $interactionSource | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
        if (isFocus) {
            Timber.d("Focused")
        }
    }

    LaunchedEffect(onOutsideBoundariesClicked) {
        Timber.d("LaunchedEffect | onOutsideBoundariesClicked: $onOutsideBoundariesClicked | coroutineContext: ${this.coroutineContext}")
        if (onOutsideBoundariesClicked) {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabTextField2(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    textFieldState: TextFieldState,
    placeholder: String,
    label: String,
    hasBorders: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    focusedBorderColor: Color = Color.Unspecified,
    unfocusedBorderColor: Color = Color.Unspecified,
    focusedContainerColor: Color = Color.Transparent,
    unfocusedContainerColor: Color = Color.Transparent,
) {

//    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState = keyboardAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocus by interactionSource.collectIsFocusedAsState()

    TheLabTheme {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .indication(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                )
                .then(modifier),
            state = textFieldState,
            textStyle = TextStyle(
                textAlign = TextAlign.Justify,
                color = Color.LightGray
            ),
            interactionSource = interactionSource,
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .apply {
                            if (hasBorders) {
                                check(Color.Unspecified != focusedBorderColor) {
                                    "You must specify a focused border color"
                                }

                                check(Color.Unspecified != unfocusedBorderColor) {
                                    "You must specify an unfocused border color"
                                }

                                val borderColorAnimation by animateColorAsState(
                                    targetValue = if (!isFocus) unfocusedBorderColor.copy(
                                        alpha = .58f
                                    ) else focusedBorderColor.copy(
                                        alpha = .75f
                                    ),
                                    label = "border animation animation"
                                )

                                this.border(
                                    width = 2.dp,
                                    color = borderColorAnimation,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = textFieldState.text.toString(),
                        placeholder = { Text(text = placeholder, color = Color.LightGray) },
                        label = { Text(text = label, color = Color.LightGray) },
                        interactionSource = interactionSource,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        leadingIcon = leadingContent,
                        trailingIcon = trailingContent,
                        innerTextField = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                innerTextField.invoke()
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = focusedContainerColor,
                            unfocusedContainerColor = unfocusedContainerColor,
                            focusedBorderColor = focusedBorderColor,
                            unfocusedBorderColor = unfocusedBorderColor
                        )
                    )
                }
            },
            readOnly = true,
            cursorBrush = SolidColor(Color.LightGray)
        )
    }

    LaunchedEffect(keyboardState) {
        Timber.d("LaunchedEffect | keyboardState shown : ${keyboardState.value} | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState.value) {
            Timber.d("KeyboardState | should show keyboard ?")
        } else {
            //show fab button
        }
    }

    LaunchedEffect(interactionSource) {
        Timber.d("LaunchedEffect | interactionSource: $interactionSource | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
        if (isFocus) {
            Timber.d("Focused")
        }
    }

    LaunchedEffect(onOutsideBoundariesClicked) {
        Timber.d("LaunchedEffect | onOutsideBoundariesClicked: $onOutsideBoundariesClicked | coroutineContext: ${this.coroutineContext}")
        /*if (onOutsideBoundariesClicked) {
          focusRster.freeFocus()
          focusManager.clearFocus()
        }*/
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabTextField2(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    query: String,
    onUpdateQuery: (String) -> Unit,
    placeholder: String,
    label: String,
    hasBorders: Boolean = false,
    focusedBorderColor: Color = Color.Unspecified,
    unfocusedBorderColor: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState = keyboardAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocus by interactionSource.collectIsFocusedAsState()

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var isFieldFocused: Boolean by remember { mutableStateOf(false) }


    TheLabTheme {
        BasicTextField(
            modifier = Modifier
                .then(modifier)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .bringIntoViewRequester(bringIntoViewRequester)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    Timber.d("Recomposition | BasicTextField2.onFocusChanged | onFocusChanged: $it")

                    if (isFieldFocused != it.isFocused) {
                        isFieldFocused = it.isFocused
                        if (!it.isFocused) {
//                                keyboardController?.hide()
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | hideKeyboard")
                            UIManager.hideKeyboard(context = context, view = view)
                        } else {
                            Timber.d("Recomposition | BasicTextField2.onFocusChanged | show keyboard")
                            scope.launch {
                                awaitFrame()
                                focusRequester.requestFocus()
                            }
                            UIManager.showKeyboard(context = context, view = view)
                            // keyboardController?.show()
                        }
                    }
                }
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        scope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .indication(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ),
            value = query,
            onValueChange = onUpdateQuery,
            textStyle = TextStyle(
                textAlign = TextAlign.Justify,
                color = Color.LightGray
            ),
            interactionSource = interactionSource,
            keyboardActions = KeyboardActions(),
            maxLines = 1,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .apply {
                            if (hasBorders) {
                                check(Color.Unspecified != focusedBorderColor) {
                                    "You must specify a focused border color"
                                }

                                check(Color.Unspecified != unfocusedBorderColor) {
                                    "You must specify an unfocused border color"
                                }

                                val borderColorAnimation by animateColorAsState(
                                    targetValue = if (!isFieldFocused) unfocusedBorderColor.copy(
                                        alpha = .58f
                                    ) else focusedBorderColor.copy(
                                        alpha = .75f
                                    ),
                                    label = "border animation animation"
                                )

                                this.border(
                                    width = 2.dp,
                                    color = borderColorAnimation,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedVisibility(
                        visible = query.isEmpty(),
                        enter = fadeIn() + slideInHorizontally(),
                        exit = slideOutHorizontally() + fadeOut()
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            text = placeholder,
                            style = TextStyle(
                                fontWeight = FontWeight.W400,
                                color = Color.LightGray.copy(alpha = .83f)
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // you have to invoke this function then cursor will focus and you will able to write something
                        innerTextField.invoke()
                    }
                }
            },
            readOnly = true,
            cursorBrush = SolidColor(Color.LightGray)
        )
    }

    LaunchedEffect(keyboardState) {
        Timber.d("LaunchedEffect | keyboardState shown : ${keyboardState.value} | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState.value) {
            Timber.d("KeyboardState | should show keyboard ?")
        } else {
            //show fab button
        }
    }

// LaunchedEffect prevents endless focus request
    LaunchedEffect(focusRequester) {
        Timber.d("LaunchedEffect | focusRequester: $focusRequester | coroutineContext: ${this.coroutineContext}")

        if (keyboardState.equals(true)) {
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
            // UIManager.showKeyboard(context = context, view = view)
        }
    }

    LaunchedEffect(interactionSource) {
        Timber.d("LaunchedEffect | interactionSource: $interactionSource | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
        if (isFocus) {
            Timber.d("Focused")
        }
    }

    LaunchedEffect(isPressed) {
        Timber.d("LaunchedEffect | isPressed: $isPressed | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
    }

    LaunchedEffect(isFocus) {
        Timber.d("LaunchedEffect | isFocus: $isFocus | coroutineContext: ${this.coroutineContext}")
        if (isFocus) {
            Timber.d("Focused")
        }
    }

    LaunchedEffect(onOutsideBoundariesClicked) {
        Timber.d("LaunchedEffect | onOutsideBoundariesClicked: $onOutsideBoundariesClicked | coroutineContext: ${this.coroutineContext}")
        if (onOutsideBoundariesClicked) {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }
}


///////////////////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewLabTextField() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF032342))
                .height(56.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = "",
                onUpdateQuery = {},
                onOutsideBoundariesClicked = false,
                placeholder = "Select an option",
                label = "Search hint"
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewLabTextField2TextFieldState() {
    val textFieldState = rememberTextFieldState()

    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF032342))
                .height(56.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabTextField2(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textFieldState = textFieldState,
                onOutsideBoundariesClicked = false,
                placeholder = "Select an option",
                label = "Search hint",
                hasBorders = true,
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.LightGray
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewLabTextField2() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF032342))
                .height(56.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabTextField2(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = "",
                onUpdateQuery = {},
                onOutsideBoundariesClicked = false,
                placeholder = "Select an option",
                label = "Search hint",
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.LightGray
            )
        }
    }
}