package com.riders.thelab.core.ui.compose.component.dropdown

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.component.toolbar.executeOnBackPressed
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.utils.findActivity
import com.riders.thelab.core.ui.compose.utils.keyboardAsState
import com.riders.thelab.core.ui.utils.UIManager
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T> LabDropdownMenu(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    query: String,
    placeholder: String,
    label: String,
    expanded: Boolean,
    readOnly: Boolean = false,
    suggestions: List<T>,
    dropdownMenuUiEvent: (UiEvent) -> Unit,
    focusedTextColor: Color = Color.Unspecified,
    unfocusedTextColor: Color = Color.Unspecified,
    disabledTextColor: Color = Color.Unspecified,
    errorTextColor: Color = Color.Unspecified,
    focusedContainerColor: Color = Color.Unspecified,
    unfocusedContainerColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    errorContainerColor: Color = Color.Unspecified,
    cursorColor: Color = Color.Unspecified,
    dropdownItemContent: @Composable (index: Int) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState by keyboardAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocus by interactionSource.collectIsFocusedAsState()

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var isFieldFocused: Boolean by remember { mutableStateOf(false) }
    var forceRequestFocus: Boolean by remember { mutableStateOf(false) }

    val borderColorAnimation by animateColorAsState(
        targetValue = if (!isFieldFocused) Color.White.copy(alpha = .58f) else Color(0xFFD37818).copy(
            alpha = .75f
        ),
        label = "border animation animation"
    )

    TheLabTheme {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = {
                Timber.d("Recomposition | ExposedDropdownMenuBox | onExpandedChange: $it")
                // onExpandedChanged(!expanded)
                if (!isFocus) {
                    Timber.d("Recomposition | ExposedDropdownMenuBox | call requestFocus")
                    forceRequestFocus = true
                }
            }
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        textFieldSize = coordinates.size.toSize()
                    }
                    .menuAnchor(type= MenuAnchorType.PrimaryEditable)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        Timber.d("Recomposition | BasicTextField2.onFocusChanged | onFocusChanged: isFieldFocused $isFieldFocused, it.isFocused: ${it.isFocused}")

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
                    .bringIntoViewRequester(bringIntoViewRequester),
                value = query,
                onValueChange = { dropdownMenuUiEvent.invoke(UiEvent.OnUpdateQuery(it)) },
                textStyle = TextStyle(
                    textAlign = TextAlign.Justify,
                    color = Color.LightGray
                ),
                placeholder = {
                    Text(
                        modifier = Modifier,
                        text = placeholder,
                        style = TextStyle(
                            fontWeight = FontWeight.W400,
                            color = Color.LightGray.copy(alpha = .83f)
                        )
                    )
                },
                label = { Text(modifier = Modifier, text = label) },
                interactionSource = interactionSource,
                keyboardActions = KeyboardActions(),
                maxLines = 1,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = focusedTextColor,
                    unfocusedTextColor = unfocusedTextColor,
                    disabledTextColor = disabledTextColor,
                    errorTextColor = errorTextColor,
                    focusedContainerColor = focusedContainerColor,
                    unfocusedContainerColor = unfocusedContainerColor,
                    disabledContainerColor = disabledContainerColor,
                    errorContainerColor = errorContainerColor,
                    cursorColor = cursorColor
                ),
                readOnly = readOnly
            )

            ExposedDropdownMenu(
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
                expanded = expanded,
                onDismissRequest = { dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false)) }
            ) {
                suggestions.forEachIndexed { itemIndex, option ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            dropdownMenuUiEvent.invoke(UiEvent.OnOptionsSelected(option))
                            dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false))
                            focusManager.clearFocus(true)
                        },
                        text = { dropdownItemContent(itemIndex) },
                        colors = MenuDefaults.itemColors(),
                        contentPadding = PaddingValues(0.dp)
                    )
                }
            }
        }
    }

    BackHandler {
        Timber.e("BackHandler()")
        if (expanded) {
            dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false))
        }
        executeOnBackPressed((context.findActivity() as BaseComponentActivity))
    }

    LaunchedEffect(forceRequestFocus) {
        Timber.d("LaunchedEffect | forceRequestFocus: $forceRequestFocus | coroutineContext: ${this.coroutineContext}")
        if (forceRequestFocus) {
            awaitFrame()
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(keyboardState) {
        Timber.d("LaunchedEffect | keyboardState shown : $keyboardState | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState) {
            Timber.i("KeyboardState | keyboard is hidden")
        } else {
            Timber.i("KeyboardState | keyboard is shown")
        }
    }

    // LaunchedEffect prevents endless focus request
    LaunchedEffect(focusRequester) {
        Timber.d("LaunchedEffect | focusRequester: $focusRequester | coroutineContext: ${this.coroutineContext}")

        if (keyboardState) {
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
fun <T> LabDropdownMenu2(
    modifier: Modifier,
    onOutsideBoundariesClicked: Boolean,
    query: String,
    placeholder: String,
    label: String,
    expanded: Boolean,
    readOnly: Boolean = false,
    suggestions: List<T>,
    dropdownMenuUiEvent: (UiEvent) -> Unit,
    dropdownItemContent: @Composable (index: Int) -> Unit
) {
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocus by interactionSource.collectIsFocusedAsState()

    val borderColorAnimation by animateColorAsState(
        targetValue = if (!isFocus) Color.White.copy(alpha = .58f) else Color(0xFFD37818).copy(
            alpha = .75f
        ),
        label = "border animation animation"
    )

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    TheLabTheme {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = {
                Timber.d("Recomposition | ExposedDropdownMenuBox | onExpandedChange: $it")
                //dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(!expanded))
                /*if (!isFocus) {
                    Timber.d("Recomposition | ExposedDropdownMenuBox | call requestFocus")
                    scope.launch {
                        forceRequestFocus = true
                        delay(100)
                        forceRequestFocus = false
                    }
                }*/
            }
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        textFieldSize = coordinates.size.toSize()
                    }
                    .menuAnchor(type= MenuAnchorType.PrimaryEditable)
                    .focusRequester(focusRequester)
                    /*.onFocusChanged {
                        Timber.d("Recomposition | BasicTextField2.onFocusChanged | onFocusChanged: isFieldFocused $isFieldFocused, it.isFocused: ${it.isFocused}")

                        if (isFieldFocused != it.isFocused) {
                            isFieldFocused = it.isFocused
                            if (!it.isFocused) {
                                Timber.d("Recomposition | BasicTextField2.onFocusChanged | hideKeyboard")
                                UIManager.hideKeyboard(context = context, view = view)
                            } else {
                                Timber.d("Recomposition | BasicTextField2.onFocusChanged | show keyboard")
                                // UIManager.showKeyboard(context = context, view = view)
                            }
                        }
                    }*/
                    /*.onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    }*/
                    .indication(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    )
//                    .bringIntoViewRequester(bringIntoViewRequester)
                ,
                value = query,
                onValueChange = { dropdownMenuUiEvent.invoke(UiEvent.OnUpdateQuery(it)) },
                textStyle = TextStyle(textAlign = TextAlign.Justify, color = Color.LightGray),
                interactionSource = interactionSource,
                keyboardActions = KeyboardActions(),
                maxLines = 1,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = borderColorAnimation,
                                shape = RoundedCornerShape(8.dp)
                            ),
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
                readOnly = readOnly,
                cursorBrush = SolidColor(Color.LightGray)
            )

            ExposedDropdownMenu(
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
                expanded = expanded,
                onDismissRequest = { dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false)) }
            ) {
                suggestions.forEachIndexed { itemIndex, option ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            dropdownMenuUiEvent.invoke(UiEvent.OnOptionsSelected(option))
                            dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false))
                            focusManager.clearFocus(true)
                        },
                        text = { dropdownItemContent(itemIndex) },
                        colors = MenuDefaults.itemColors(),
                        contentPadding = PaddingValues(0.dp)
                    )
                }
            }
        }
    }

    BackHandler {
        Timber.e("BackHandler()")
        if (expanded) {
            dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false))
        }
        executeOnBackPressed((context.findActivity() as BaseComponentActivity))
    }

    /*LaunchedEffect(interactionSource) {
        Timber.i("LaunchedEffect | interactionSource: $interactionSource | coroutineContext: ${this.coroutineContext}")

        if (isPressed) {
            Timber.d("Pressed")
        }
        if (isFocus) {
            Timber.d("Focused")
        }
    }


    LaunchedEffect(forceRequestFocus) {
        Timber.d("LaunchedEffect | forceRequestFocus: $forceRequestFocus | coroutineContext: ${this.coroutineContext}")
        if (forceRequestFocus) {
            awaitFrame()
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(keyboardState) {
        Timber.i("LaunchedEffect | keyboardState shown : $keyboardState | coroutineContext: ${this.coroutineContext}")
        if (!keyboardState) {
            Timber.i("KeyboardState | keyboard is hidden")
        } else {
            Timber.i("KeyboardState | keyboard is shown")
        }
    }*/

    // LaunchedEffect prevents endless focus request
    /*LaunchedEffect(focusRequester) {
        Timber.d("LaunchedEffect | focusRequester: $focusRequester | coroutineContext: ${this.coroutineContext}")

        if (keyboardState.equals(true)) {
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
            // UIManager.showKeyboard(context = context, view = view)
        }
    }*/

    LaunchedEffect(onOutsideBoundariesClicked) {
        Timber.d("LaunchedEffect | onOutsideBoundariesClicked: $onOutsideBoundariesClicked | coroutineContext: ${this.coroutineContext}")
        if (onOutsideBoundariesClicked) {
            /*focusRequester.freeFocus()
            focusManager.clearFocus()*/

            if (expanded) {
                dropdownMenuUiEvent.invoke(UiEvent.OnExpandedChanged(false))
            }
        }
    }

    /*SideEffect*//*(forceRequestFocus)*//* {
        Timber.w("SideEffect | isFocus: $isFocus, forceRequestFocus: $forceRequestFocus")
        if (!isFocus && forceRequestFocus) {
            // awaitFrame()
            focusRequester.requestFocus()
        }
    }*/
}


@DevicePreviews
@Composable
private fun PreviewLabDropdownMenu() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF032342))
                .height(40.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabDropdownMenu<List<String>>(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = "Pi'erre",
                expanded = true,
                onOutsideBoundariesClicked = false,
                placeholder = "Select an option",
                label = "Search hint",
                suggestions = listOf(),
                dropdownMenuUiEvent = {},
                dropdownItemContent = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PreviewLabDropdownMenu2() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF032342))
                .height(40.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabDropdownMenu2(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = "Pi'erre",
                expanded = true,
                onOutsideBoundariesClicked = false,
                placeholder = "Select an option",
                label = "Search hint",
                suggestions = listOf("test", "mike", "chronopost", "john", "None"),
                dropdownMenuUiEvent = {},
                dropdownItemContent = {}
            )
        }
    }
}