package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsBottomSheet(
    state: ReviewsUiState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    if (state is ReviewsUiState.Hidden) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = EduColors.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            val product = when (state) {
                is ReviewsUiState.Loading -> state.product
                is ReviewsUiState.Ready -> state.product
                is ReviewsUiState.Error -> state.product
                ReviewsUiState.Hidden -> null
            }
            if (product != null) {
                Text(
                    "Avaliações",
                    style = MaterialTheme.typography.titleLarge,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextSecondary,
                )
                Spacer(Modifier.height(8.dp))
                RatingStars(
                    rating = product.ratingAvg,
                    count = product.ratingCount,
                    starSize = 18.dp,
                )
                Spacer(Modifier.height(16.dp))
            }

            when (state) {
                is ReviewsUiState.Loading -> Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = EduColors.Primary)
                }
                is ReviewsUiState.Error -> Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = EduColors.TextSecondary)
                    TextButton(onClick = onRetry) {
                        Text("Tentar novamente", color = EduColors.Primary)
                    }
                }
                is ReviewsUiState.Ready -> {
                    if (state.reviews.isEmpty()) {
                        Text(
                            "Este produto ainda não possui avaliações.",
                            color = EduColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp),
                        ) {
                            items(state.reviews, key = { it.id }) { review ->
                                ReviewItem(review)
                            }
                        }
                    }
                }
                ReviewsUiState.Hidden -> Unit
            }
        }
    }
}

