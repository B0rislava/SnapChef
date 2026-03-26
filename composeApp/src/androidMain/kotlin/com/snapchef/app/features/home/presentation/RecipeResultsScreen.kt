package com.snapchef.app.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary

@Composable
fun RecipeResultsScreen(
    ingredients: List<String>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSecondary.copy(alpha = 0.3f))
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.10f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Recipe Magic",
                style = MaterialTheme.typography.headlineMedium,
                color = GreenPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info card showing what we used
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GreenBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Generated using:",
                    style = MaterialTheme.typography.titleMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val ingredientText = ingredients.joinToString(separator = " • ")
                Text(
                    text = ingredientText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenOnBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Suggested Recipes",
            style = MaterialTheme.typography.titleLarge,
            color = GreenPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder lazy column for future recipe results
         LazyColumn(
             modifier = Modifier.fillMaxSize(),
             verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
             items(3) { index ->
                 Card(
                     modifier = Modifier.fillMaxWidth().height(140.dp),
                     shape = RoundedCornerShape(24.dp),
                     colors = CardDefaults.cardColors(containerColor = Color.White),
                     elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                 ) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text(
                             text = "Recipe Result ${index + 1}\n(Coming Soon)", 
                             color = GreenPrimary,
                             fontWeight = FontWeight.Bold,
                             textAlign = androidx.compose.ui.text.style.TextAlign.Center
                         )
                     }
                 }
             }
         }
    }
}
