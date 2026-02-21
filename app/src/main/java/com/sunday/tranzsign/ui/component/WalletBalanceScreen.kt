package com.sunday.tranzsign.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sunday.tranzsign.R

@Composable
fun WalletBalanceScreen(
    introTitle: String,
    balance: String,
    lastUpdated: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enableRefresh: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = introTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = balance,
                    modifier = Modifier,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold
                )
                FilledTonalIconButton(
                    onClick = onRefresh,
                    enabled = enableRefresh,
                    shape = CircleShape,
                ) {
                    Icon(
                        modifier = Modifier.padding(2.dp),
                        painter = painterResource(R.drawable.ic_refresh_24),
                        contentDescription = stringResource(R.string.refresh_balance_label)
                    )
                }
            }

            Text(
                text = lastUpdated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Thin
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWalletBalanceScreen() {
    MaterialTheme {
        WalletBalanceScreen(
            introTitle = "--",
            balance = "--",
            lastUpdated = "--",
            onRefresh = {}
        )
    }
}
