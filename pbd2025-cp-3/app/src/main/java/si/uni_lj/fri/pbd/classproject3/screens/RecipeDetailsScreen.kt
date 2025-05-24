package si.uni_lj.fri.pbd.classproject3.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import si.uni_lj.fri.pbd.classproject3.R
import si.uni_lj.fri.pbd.classproject3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.classproject3.viewmodels.DetailsViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    startedFromSearchScreen: Boolean,
    detailsViewModel: DetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by detailsViewModel.uiState.collectAsState()

    LaunchedEffect(recipeId, startedFromSearchScreen) {
        detailsViewModel.fetchRecipeDetails(recipeId, startedFromSearchScreen)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            detailsViewModel.errorMessageShown() // Reset error message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.recipeDetails?.strMeal ?: "Recipe Details",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.recipeDetails?.let { // Show favorite button only if details are loaded
                        IconButton(onClick = { detailsViewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.recipeDetails != null -> {
                RecipeDetailsContent(
                    recipe = uiState.recipeDetails!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> { // Error or no data
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Could not load recipe details.")
                }
            }
        }
    }
}

@Composable
fun RecipeDetailsContent(recipe: RecipeDetailsIM, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make content scrollable
            .padding(16.dp)
    ) {
        // Recipe Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(recipe.strMealThumb)
                .crossfade(true)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_error_image)
                .build(),
            contentDescription = recipe.strMeal,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f) // Standard image aspect ratio
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            text = recipe.strMeal ?: "Unnamed Recipe",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Category and Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            recipe.strCategory?.let {
                Text("Category: $it", style = MaterialTheme.typography.bodyLarge)
            }
            recipe.strArea?.let {
                Text("Area: $it", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients and Measures
        Text("Ingredients:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        formatIngredientsAndMeasures(recipe).forEach { (ingredient, measure) ->
            if (ingredient.isNotBlank()) {
                Text(
                    text = "• $ingredient" + if (measure.isNotBlank()) " ($measure)" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Text("Instructions:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = recipe.strInstructions?.trim() ?: "No instructions provided.",
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )

        // YouTube Link
        recipe.strYoutube?.takeIf { it.isNotBlank() }?.let { youtubeUrl ->
            Spacer(modifier = Modifier.height(24.dp))
            Text("Watch on YouTube:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = youtubeUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, youtubeUrl.toUri())
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("RecipeDetailsScreen", "Error opening YouTube link", e)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Helper function to parse and combine ingredients and their measures from RecipeDetailsIM.
 * The API stores ingredients and measures in separate fields (strIngredient1, strMeasure1, etc.).
 */
fun formatIngredientsAndMeasures(recipe: RecipeDetailsIM): List<Pair<String, String>> {
    val ingredients = mutableListOf<Pair<String, String>>()
    val fields = recipe.javaClass.declaredFields
    val measuresMap = mutableMapOf<String, String>()

    // Collect all measures with their corresponding number
    for (i in 1..20) {
        val measureField = fields.firstOrNull { it.name == "strMeasure$i" }
        measureField?.isAccessible = true
        val measureValue = (measureField?.get(recipe) as? String)?.trim() ?: ""
        if (measureValue.isNotBlank()) {
            measuresMap["strIngredient$i"] = measureValue
        }
    }

    // Collect ingredients and pair them with their measures
    for (i in 1..20) {
        val ingredientField = fields.firstOrNull { it.name == "strIngredient$i" }
        ingredientField?.isAccessible = true
        val ingredientValue = (ingredientField?.get(recipe) as? String)?.trim() ?: ""

        if (ingredientValue.isNotBlank()) {
            val measure = measuresMap["strIngredient$i"] ?: ""
            ingredients.add(Pair(ingredientValue, measure))
        }
    }

    return ingredients
}

