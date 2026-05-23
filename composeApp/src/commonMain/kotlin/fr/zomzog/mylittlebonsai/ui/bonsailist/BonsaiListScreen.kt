package fr.zomzog.mylittlebonsai.ui.bonsailist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository

const val BONSAI_LIST_TITLE = "My Bonsais"
const val ADD_BONSAI_BUTTON_DESCRIPTION = "Add bonsai"
const val EMPTY_LIST_MESSAGE = "No bonsais yet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonsaiListScreen(
    repository: BonsaiRepository,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bonsais by repository.getBonsaisStream().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BONSAI_LIST_TITLE) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateToAdd,
                        modifier = Modifier.semantics { contentDescription = ADD_BONSAI_BUTTON_DESCRIPTION },
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        if (bonsais.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(EMPTY_LIST_MESSAGE)
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(bonsais, key = { it.id }) { bonsai ->
                    BonsaiCard(bonsai = bonsai)
                }
            }
        }
    }
}

@Composable
private fun BonsaiCard(bonsai: Bonsai, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(bonsai.name, style = MaterialTheme.typography.titleMedium)
            Text(bonsai.kind, style = MaterialTheme.typography.bodyMedium)
            Text(bonsai.purchaseDate.toString(), style = MaterialTheme.typography.bodySmall)
            bonsai.lastMaintenanceDate?.let { date ->
                Text(date.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
