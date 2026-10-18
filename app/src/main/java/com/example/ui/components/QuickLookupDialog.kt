package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.api.DictionaryResult
import com.example.core.api.WikiSummary
import com.example.ui.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLookupModal(
    isOpen: Boolean,
    query: String,
    isLoading: Boolean,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    dictionaryResult: DictionaryResult?,
    wikiResult: WikiSummary?,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSpeakWord: (String) -> Unit,
    onImportToNotes: (String, String) -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val isBn = currentLanguage == AppLanguage.BENGALI
    var textInput by remember(query) { mutableStateOf(query) }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "বাংলা উইকি ও অভিধান" else "OmniLookup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "বাংলা উইকিপিডিয়া ও ফ্রি ডিকশনারি এপিআই (সম্পূর্ণ ফ্রি)" else "Free Dictionary & Wikipedia REST APIs (Zero Cost)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        onQueryChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lookup_search_input"),
                    placeholder = {
                        Text(if (isBn) "যেকোনো Term বা Concept সার্চ করো (e.g. Gravity, Entropy)..." else "Look up term (e.g. Entropy, Mitochondria, Heuristic)...")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { onSearch(textInput) },
                            modifier = Modifier.testTag("lookup_search_action")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Wikipedia Section
                        if (wikiResult != null) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${if (wikiResult.language == "bn") "বাংলা উইকিপিডিয়া" else "Wikipedia"}: ${wikiResult.title}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            IconButton(onClick = { onSpeakWord(wikiResult.extract) }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Read Summary",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        if (wikiResult.description.isNotBlank()) {
                                            Text(
                                                text = wikiResult.description,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = wikiResult.extract,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                onImportToNotes(wikiResult.title, wikiResult.extract)
                                                onClose()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text(if (isBn) "Cornell Notes-এ Import করো (+60 XP)" else "Import into Cornell Notes (+60 XP)")
                                        }
                                    }
                                }
                            }
                        }

                        // Dictionary Section
                        if (dictionaryResult != null) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = dictionaryResult.word.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (dictionaryResult.phonetic.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = dictionaryResult.phonetic,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                }
                                            }
                                            IconButton(onClick = { onSpeakWord(dictionaryResult.word) }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Pronounce Word",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        dictionaryResult.meanings.forEach { meaning ->
                                            Text(
                                                text = meaning.partOfSpeech.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            meaning.definitions.take(2).forEachIndexed { idx, def ->
                                                Text(
                                                    text = "${idx + 1}. ${def.definition}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                if (def.example != null) {
                                                    Text(
                                                        text = "\"${def.example}\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontStyle = FontStyle.Italic,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (dictionaryResult == null && wikiResult == null && !isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBn) "যেকোনো Term বা Scientific Concept লিখে Search করো। এটি 100% Free।" else "Search any word or scientific concept for instant definitions and Wikipedia context. Completely free.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
