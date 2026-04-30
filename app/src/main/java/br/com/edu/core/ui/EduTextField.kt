package br.com.edu.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EduTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                color = EduColors.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
        },
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        singleLine = singleLine,
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        textStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(color = EduColors.TextPrimary),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = EduColors.InputFill,
            unfocusedContainerColor = EduColors.InputFill,
            disabledContainerColor = EduColors.InputFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = EduColors.Purple,
            focusedTextColor = EduColors.TextPrimary,
            unfocusedTextColor = EduColors.TextPrimary,
        ),
    )
}
