package com.snapchef.app.features.home.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGenerateRecipes: (List<String>) -> Unit,
    isCameraActive: Boolean,
    onCameraActiveChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var showModal by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var ingredients by remember { mutableStateOf(listOf<String>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission and Camera States
    var showRationale by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Logic to detect "Don't ask anymore" or repeated denial
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    // Check permissions when coming back from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = isGranted
                if (isGranted) {
                    isPermanentlyDenied = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            onCameraActiveChanged(true)
            isPermanentlyDenied = false
        } else {
            // Check if we should show rationale now
            val activity = context as? Activity
            if (activity != null) {
                isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            }
        }
    }

    // Permission Rationale Dialog
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { 
                Text(
                    text = if (isPermanentlyDenied) "Camera Access Required" else "Camera Access",
                    fontWeight = FontWeight.Bold 
                ) 
            },
            text = { 
                Text(
                    text = if (isPermanentlyDenied) {
                        "You've disabled camera access. To recognize ingredients, please enable it in your app settings."
                    } else {
                        "SnapChef needs camera access to instantly recognize your ingredients and suggest the best recipes."
                    }
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationale = false
                        if (isPermanentlyDenied) {
                            // Open App Settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        text = if (isPermanentlyDenied) "Open Settings" else "Allow",
                        color = Color.White 
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Later", color = GreenPrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.45f), GreenBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCameraActive && hasCameraPermission) {
            // Camera Preview Overlay
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (e: Exception) {
                                Log.e("CameraX", "Binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Capture Button Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            onCameraActiveChanged(false)
                            coroutineScope.launch {
                                ingredients = emptyList()
                                isAnalyzing = true
                                showModal = true
                                delay(1500)
                                ingredients = listOf("Tomatoes", "Eggs", "Cheese", "Onion")
                                isAnalyzing = false
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(GreenPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap to recognize ingredients",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Close Camera Button
                IconButton(
                    onClick = { onCameraActiveChanged(false) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .statusBarsPadding()
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                }
            }
        } else {
            // Standard Dashboard
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.15f))
                        .clickable {
                            if (hasCameraPermission) {
                                onCameraActiveChanged(true)
                            } else {
                                showRationale = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Snap Ingredients",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Snap your ingredients",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Take a photo of the food in your kitchen.\nWe'll recognize what you have and generate delicious recipes instantly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GreenOnBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Recognition Modal Bottom Sheet
        if (showModal) {
            ModalBottomSheet(
                onDismissRequest = { showModal = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = GreenSecondary) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAnalyzing) "Analyzing Photo..." else "Ingredients Found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = isAnalyzing,
                        label = "analysis state",
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }
                    ) { analyzing ->
                        if (analyzing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = GreenPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Identifying food items with AI...", color = GreenOnBackground.copy(alpha = 0.6f))
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Confirm the items below, or remove any mistakes before generating recipes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 280.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(ingredients) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(GreenBackground)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = GreenPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = GreenOnBackground
                                                )
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    ingredients = ingredients.filter { it != item }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "Remove $item",
                                                    tint = GreenSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                var newIngredient by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newIngredient,
                                        onValueChange = { newIngredient = it },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        placeholder = { Text("Add missing ingredient") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenPrimary,
                                            unfocusedBorderColor = GreenSecondary.copy(alpha = 0.4f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            if (newIngredient.isNotBlank()) {
                                                ingredients = ingredients + newIngredient.trim()
                                                newIngredient = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(GreenPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showModal = false
                                            onGenerateRecipes(ingredients)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    enabled = ingredients.isNotEmpty()
                                ) {
                                    Text(
                                        text = "Generate Recipes",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
