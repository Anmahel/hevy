package com.example.gymtracker.ui.components.media

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

/**
 * ImageLoader global configurado para decodificar animaciones GIF en bucle continuo:
 * - ImageDecoderDecoder para Android 9.0+ (API >= 28)
 * - GifDecoder tradicional para versiones anteriores (API < 28)
 */
@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                coil.memory.MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("exercise_image_cache"))
                    .maxSizeBytes(128L * 1024 * 1024) // 128 MB de caché persistente para el gimnasio
                    .build()
            }
            .crossfade(true)
            .build()
    }
}

/**
 * Normaliza la fuente del GIF para soportar:
 * - Rutas en assets (ej: "file:///android_asset/gifs/...", "gifs/bench_press.gif", "bench_press.gif")
 * - URLs remotas (ej: "https://.../exercise.gif")
 */
fun normalizeMediaSource(source: String): String {
    if (source.isBlank()) return ""
    return when {
        source.startsWith("data:image") -> source
        source.startsWith("http://") || source.startsWith("https://") -> source
        source.startsWith("file://") || source.startsWith("content://") -> source
        source.startsWith("android.resource://") -> source
        source.startsWith("/") -> "file://$source"
        source.startsWith("gifs/") -> "file:///android_asset/$source"
        source.startsWith("/gifs/") -> "file:///android_asset$source"
        source.startsWith("assets/") -> "file:///android_asset/${source.removePrefix("assets/")}"
        source.endsWith(".gif") || source.endsWith(".png") || source.endsWith(".jpg") || source.endsWith(".webp") -> {
            "file:///android_asset/gifs/$source"
        }
        else -> source
    }
}

fun resolveCoilDataModel(normalizedSource: String): Any {
    if (normalizedSource.startsWith("data:image") && normalizedSource.contains(";base64,")) {
        return try {
            val base64Part = normalizedSource.substringAfter(";base64,")
            android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
        } catch (_: Exception) {
            normalizedSource
        }
    }
    return normalizedSource
}

/**
 * Reproductor de GIFs animado en bucle continuo para la vista de detalle del ejercicio.
 * Muestra placeholder oscuro con indicador circular mientras carga, y vista estilizada si no hay GIF.
 */
@Composable
fun ExerciseGifPlayer(
    gifSource: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Demostración animada del ejercicio",
    exerciseName: String? = null
) {
    val context = LocalContext.current
    val imageLoader = rememberGifImageLoader()
    val resolvedSource = remember(gifSource, exerciseName) {
        val direct = normalizeMediaSource(gifSource)
        if (direct.isNotBlank()) direct else {
            val fallback = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise(exerciseName ?: "")
            normalizeMediaSource(fallback)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (resolvedSource.isNotBlank()) {
            val imageRequest = remember(resolvedSource) {
                ImageRequest.Builder(context)
                    .data(resolveCoilDataModel(resolvedSource))
                    .crossfade(300)
                    .build()
            }

            SubcomposeAsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                },
                error = {
                    ExercisePlayerPlaceholder(exerciseName = exerciseName)
                }
            )
        } else {
            ExercisePlayerPlaceholder(exerciseName = exerciseName)
        }
    }
}

/**
 * Miniatura circular estilizada idéntica a Hevy:
 * Recortada en CircleShape con fondo blanco limpio para modelos 3D y Coil AsyncImage (ContentScale.Crop).
 */
@Composable
fun ExerciseThumbnail(
    imageSource: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentDescription: String? = "Miniatura del ejercicio"
) {
    val context = LocalContext.current
    val imageLoader = rememberGifImageLoader()
    val normalizedSource = remember(imageSource) { normalizeMediaSource(imageSource) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (normalizedSource.isNotBlank()) Color.White else Color(0xFF1E1E22))
            .border(1.dp, Color(0xFF2C2C34), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (normalizedSource.isNotBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolveCoilDataModel(normalizedSource))
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF161618)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(size * 0.45f)
                        )
                    }
                },
                error = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = contentDescription,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier.size(size * 0.52f)
                    )
                }
            )
        } else {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

/**
 * Placeholder elegante estilo Hevy Dark cuando un ejercicio no tiene animación cargada todavía
 */
@Composable
fun ExercisePlayerPlaceholder(
    exerciseName: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1C1C22),
                        Color(0xFF121216)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "GUÍA DE EJECUCIÓN ANATÓMICA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exerciseName ?: "Demostración Técnica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ilustración de biomecánica y recorrido",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
