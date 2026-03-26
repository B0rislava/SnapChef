package com.snapchef.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapchef.app.ui.theme.GreenOnBackground
import com.snapchef.app.ui.theme.GreenPrimary
import com.snapchef.app.ui.theme.GreenSecondary

// ─── Reusable text field ──────────────────────────────────────────────────────
@Composable
fun AuthTextField(
    modifier:             Modifier = Modifier,
    value:                String,
    onValueChange:        (String) -> Unit,
    placeholder:          String,
    leadingIcon:          (@Composable () -> Unit)? = null,
    trailingIcon:         (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType:         KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        placeholder          = {
            Text(
                text  = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = GreenOnBackground.copy(alpha = 0.38f),
            )
        },
        leadingIcon          = leadingIcon,
        trailingIcon         = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions      = KeyboardOptions(keyboardType = keyboardType),
        singleLine           = true,
        shape                = RoundedCornerShape(14.dp),
        colors               = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GreenPrimary,
            unfocusedBorderColor = GreenSecondary,
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor          = GreenPrimary,
            focusedTextColor     = GreenOnBackground,
            unfocusedTextColor   = GreenOnBackground,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ─── "Or" divider ─────────────────────────────────────────────────────────────
@Composable
fun OrDivider() {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = GreenSecondary,
            thickness = 1.dp,
        )
        Text(
            text      = "  Or  ",
            style     = MaterialTheme.typography.bodyMedium,
            color     = GreenOnBackground.copy(alpha = 0.45f),
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = GreenSecondary,
            thickness = 1.dp,
        )
    }
}

// ─── Social button ────────────────────────────────────────────────────────────
@Composable
fun SocialButton(
    label:   String,
    emoji:   String,
    onClick: () -> Unit = {},
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor   = GreenOnBackground,
        ),
        border   = BorderStroke(1.5.dp, GreenSecondary),
    ) {
        Text(
            text       = emoji,
            fontSize   = 18.sp,
            modifier   = Modifier.padding(end = 10.dp),
        )
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = GreenOnBackground,
        )
    }
}
