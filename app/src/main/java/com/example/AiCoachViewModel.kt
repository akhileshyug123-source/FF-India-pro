package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiCoachViewModel"

sealed interface AiCoachUiState {
  data object Idle : AiCoachUiState

  data object Loading : AiCoachUiState

  data class Streaming(val text: String) : AiCoachUiState

  data class Success(val text: String) : AiCoachUiState

  data class Error(val message: String) : AiCoachUiState
}

class AiCoachViewModel : ViewModel() {
  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      systemInstruction =
        content {
          text(
            "You are the FF India Pro Tactical Gaming Coach. Give practical, legitimate, " +
              "fair-play advice on sensitivity tuning, safe Android DPI ranges, multi-finger HUD ergonomics, " +
              "drag-up headshot timing, weapon recoil/range matchups, and training drills. " +
              "Never suggest cheats, injectors, hacks, or unauthorized game modifications. " +
              "Keep answers crisp, scannable, and bulleted for mobile reading."
          )
        },
    )
  }

  private val _uiState = MutableStateFlow<AiCoachUiState>(AiCoachUiState.Idle)
  val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

  private val _playerContext = MutableStateFlow("")
  val playerContext: StateFlow<String> = _playerContext.asStateFlow()

  fun updatePlayerContext(contextSummary: String) {
    _playerContext.value = contextSummary
  }

  fun askCoach(userPrompt: String) {
    val trimmed = userPrompt.trim()
    if (trimmed.isEmpty()) return

    val fullPrompt =
      if (_playerContext.value.isNotBlank()) {
        "Player Current Panel Config: ${_playerContext.value}\n\nQuestion: $trimmed"
      } else {
        trimmed
      }

    viewModelScope.launch {
      _uiState.value = AiCoachUiState.Loading
      val accumulatedText = StringBuilder()
      try {
        withTimeout(60_000L) {
          model.generateContentStream(fullPrompt).collect { chunk ->
            val chunkText = chunk.text.orEmpty()
            if (chunkText.isNotEmpty()) {
              accumulatedText.append(chunkText)
              _uiState.value = AiCoachUiState.Streaming(accumulatedText.toString())
            }
          }
        }
        if (accumulatedText.isNotBlank()) {
          _uiState.value = AiCoachUiState.Success(accumulatedText.toString())
        } else {
          _uiState.value = AiCoachUiState.Error("Gemini returned an empty response.")
        }
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Gemini AI request timed out", e)
        _uiState.value =
          AiCoachUiState.Error("Request timed out after 60 seconds. Please try again.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Firebase AI generation failed", e)
        _uiState.value =
          AiCoachUiState.Error(e.localizedMessage ?: "Failed to get response from Gemini.")
      }
    }
  }

  fun clearResponse() {
    _uiState.value = AiCoachUiState.Idle
  }
}
