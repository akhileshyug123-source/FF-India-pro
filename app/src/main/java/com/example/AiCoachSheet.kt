package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TacticalOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

private val QUICK_PROMPTS =
  listOf(
    "Optimize my Sensitivity & DPI",
    "One-Tap M1887 Drag Tips",
    "Best 3-Finger Claw Setup",
    "Counter MP40 Close Rush",
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachSheet(
  uiState: AiCoachUiState,
  playerContext: String,
  onDismiss: () -> Unit,
  onAskPrompt: (String) -> Unit,
  onClear: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var promptText by rememberSaveable { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SurfaceDark,
    contentColor = TextPrimary,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Gemini AI Coach",
            tint = NeonCyan,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Gemini Tactical AI Coach",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimary,
            )
            Text(
              text = "Powered by Firebase AI Logic",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
            )
          }
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_ai_coach_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close AI Coach",
            tint = TextMuted,
          )
        }
      }

      if (playerContext.isNotBlank()) {
        Surface(
          color = SurfaceCard,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
        ) {
          Text(
            text = "Synced Stats: $playerContext",
            style = MaterialTheme.typography.bodySmall,
            color = NeonGreen,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QUICK_PROMPTS.forEachIndexed { index, quick ->
          AssistChip(
            onClick = {
              promptText = quick
              onAskPrompt(quick)
            },
            label = { Text(quick) },
            colors =
              AssistChipDefaults.assistChipColors(
                containerColor = SurfaceCard,
                labelColor = TextPrimary,
              ),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
            modifier = Modifier.testTag("ai_quick_prompt_$index"),
          )
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBg),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.22f)),
        shape = RoundedCornerShape(14.dp),
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth().padding(14.dp).verticalScroll(rememberScrollState()),
        ) {
          when (uiState) {
            is AiCoachUiState.Idle -> {
              Text(
                text =
                  "Ask for sensitivity calibration, DPI recommendations, weapon loadouts, " +
                    "or drag-up headshot drills tailored to your current panel stats.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
              )
            }
            is AiCoachUiState.Loading -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(22.dp),
                  color = NeonCyan,
                  strokeWidth = 2.5.dp,
                )
                Text(
                  text = "Analyzing setup with Gemini...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = NeonCyan,
                )
              }
            }
            is AiCoachUiState.Streaming -> {
              Text(
                text = uiState.text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
              )
            }
            is AiCoachUiState.Success -> {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = uiState.text,
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextPrimary,
                )
                Button(
                  onClick = onClear,
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = SurfaceCard,
                      contentColor = NeonCyan,
                    ),
                  modifier = Modifier.testTag("clear_ai_response_button"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "New Question",
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Ask Another Question")
                }
              }
            }
            is AiCoachUiState.Error -> {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = uiState.message,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFFF6B81),
                )
                Button(
                  onClick = { if (promptText.isNotBlank()) onAskPrompt(promptText) },
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = TacticalOrange,
                      contentColor = ObsidianBg,
                    ),
                  modifier = Modifier.testTag("retry_ai_button"),
                ) {
                  Text("Retry Request")
                }
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = promptText,
          onValueChange = { promptText = it },
          placeholder = { Text("Ask Gemini AI Coach...", color = TextMuted) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions =
            KeyboardActions(
              onSend = {
                if (promptText.isNotBlank() && uiState !is AiCoachUiState.Loading) {
                  onAskPrompt(promptText)
                }
              },
            ),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = NeonCyan.copy(alpha = 0.35f),
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              cursorColor = NeonCyan,
            ),
          modifier = Modifier.weight(1f).testTag("ai_prompt_input"),
        )
        Button(
          onClick = { onAskPrompt(promptText) },
          enabled = promptText.isNotBlank() && uiState !is AiCoachUiState.Loading,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = NeonCyan,
              contentColor = ObsidianBg,
            ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.height(52.dp).testTag("send_ai_prompt_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send prompt to Gemini",
          )
        }
      }
    }
  }
}
