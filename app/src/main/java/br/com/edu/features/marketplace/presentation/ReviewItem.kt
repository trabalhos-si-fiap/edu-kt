package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.ui.EduCard
import br.com.edu.features.marketplace.domain.Review

@Composable
fun ReviewItem(review: Review) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        radius = 14.dp,
        shadow = 1.dp,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    review.author,
                    style = MaterialTheme.typography.titleSmall,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                RatingStars(
                    rating = review.rating.toDouble(),
                    count = 0,
                    showCount = false,
                    starSize = 14.dp,
                )
            }
            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextSecondary,
                )
            }
        }
    }
}
