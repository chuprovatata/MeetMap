package com.meetmap.datingapp.viewmodels

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.meetmap.datingapp.data.models.EventDateSlot
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.repository.EventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class CreateEventDateUi(
    val dateFrom: String = "",
    val dateTo: String = "",
    val startTime: String = "",
    val dateFromError: String? = null,
    val dateToError: String? = null,
    val startTimeError: String? = null
)

data class CreateEventFieldErrors(
    val title: String? = null,
    val dates: String? = null,
    val duration: String? = null,
    val address: String? = null,
    val sourceUrl: String? = null,
    val description: String? = null,
    val ageLimit: String? = null,
    val participantNotes: String? = null,
    val moderatorNotes: String? = null
)

data class CreateEventUiState(
    val eventId: String = "",
    val photoUri: Uri? = null,
    val photoUrl: String = "",
    val title: String = "",
    val dates: List<CreateEventDateUi> = listOf(CreateEventDateUi()),
    val duration: String = "",
    val address: String = "",
    val isOrganizer: Boolean = false,
    val sourceUrl: String = "",
    val description: String = "",
    val ageLimit: String = "",
    val participantNotes: String = "",
    val moderatorNotes: String = "",
    val notifyAboutModeration: Boolean = true,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val submittedEventId: String? = null,
    val errors: CreateEventFieldErrors = CreateEventFieldErrors()
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun setPhotoUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun setTitle(value: String) {
        _uiState.value = _uiState.value.copy(
            title = value.take(TITLE_MAX),
            errors = _uiState.value.errors.copy(title = null)
        )
    }

    fun setDuration(value: String) {
        _uiState.value = _uiState.value.copy(
            duration = value.toTimeMask(),
            errors = _uiState.value.errors.copy(duration = null)
        )
    }

    fun setAddress(value: String) {
        _uiState.value = _uiState.value.copy(
            address = value.take(ADDRESS_MAX),
            errors = _uiState.value.errors.copy(address = null)
        )
    }

    fun setIsOrganizer(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isOrganizer = value,
            errors = _uiState.value.errors.copy(sourceUrl = null)
        )
    }

    fun setSourceUrl(value: String) {
        _uiState.value = _uiState.value.copy(
            sourceUrl = value.take(SOURCE_URL_MAX),
            errors = _uiState.value.errors.copy(sourceUrl = null)
        )
    }

    fun setDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value.take(DESCRIPTION_MAX),
            errors = _uiState.value.errors.copy(description = null)
        )
    }

    fun setAgeLimit(value: String) {
        _uiState.value = _uiState.value.copy(
            ageLimit = value.filter { it.isDigit() }.take(2),
            errors = _uiState.value.errors.copy(ageLimit = null)
        )
    }

    fun setParticipantNotes(value: String) {
        _uiState.value = _uiState.value.copy(
            participantNotes = value.take(PARTICIPANT_NOTES_MAX),
            errors = _uiState.value.errors.copy(participantNotes = null)
        )
    }

    fun setModeratorNotes(value: String) {
        _uiState.value = _uiState.value.copy(
            moderatorNotes = value.take(MODERATOR_NOTES_MAX),
            errors = _uiState.value.errors.copy(moderatorNotes = null)
        )
    }

    fun setNotifyAboutModeration(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyAboutModeration = value)
    }

    fun setDateFrom(index: Int, value: String) {
        updateDate(index) {
            it.copy(
                dateFrom = value.toDateMask(),
                dateFromError = null
            )
        }
    }

    fun setDateTo(index: Int, value: String) {
        updateDate(index) {
            it.copy(
                dateTo = value.toDateMask(),
                dateToError = null
            )
        }
    }

    fun setStartTime(index: Int, value: String) {
        updateDate(index) {
            it.copy(
                startTime = value.toTimeMask(),
                startTimeError = null
            )
        }
    }

    fun addDateSlot() {
        _uiState.value = _uiState.value.copy(
            dates = _uiState.value.dates + CreateEventDateUi(),
            errors = _uiState.value.errors.copy(dates = null)
        )
    }

    fun removeDateSlot(index: Int) {
        val current = _uiState.value.dates

        if (current.size <= 1) return

        _uiState.value = _uiState.value.copy(
            dates = current.filterIndexed { i, _ -> i != index }
        )
    }

    private fun updateDate(
        index: Int,
        update: (CreateEventDateUi) -> CreateEventDateUi
    ) {
        val current = _uiState.value.dates.toMutableList()

        if (index !in current.indices) return

        current[index] = update(current[index])

        _uiState.value = _uiState.value.copy(
            dates = current,
            errors = _uiState.value.errors.copy(dates = null)
        )
    }

    fun loadEventForEdit(eventId: String) {
        if (eventId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            eventsRepository.getEventById(eventId)
                .onSuccess { event ->
                    _uiState.value = _uiState.value.copy(
                        eventId = event.id,
                        photoUri = null,
                        photoUrl = event.photoUrl,
                        title = event.title,
                        dates = if (event.dates.isNotEmpty()) {
                            event.dates.map { slot ->
                                CreateEventDateUi(
                                    dateFrom = slot.dateFrom,
                                    dateTo = slot.dateTo,
                                    startTime = slot.startTime
                                )
                            }
                        } else {
                            listOf(CreateEventDateUi())
                        },
                        duration = event.duration,
                        address = event.address,
                        isOrganizer = event.isOrganizer,
                        sourceUrl = event.sourceUrl,
                        description = event.description,
                        ageLimit = event.ageLimit?.toString().orEmpty(),
                        participantNotes = event.participantNotes,
                        moderatorNotes = event.moderatorNotes,
                        notifyAboutModeration = event.notifyAboutModeration,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось загрузить мероприятие"
                    )
                }
        }
    }

    fun hasDraftContent(): Boolean {
        val state = _uiState.value

        return state.photoUri != null ||
                state.photoUrl.isNotBlank() ||
                state.title.isNotBlank() ||
                state.dates.any {
                    it.dateFrom.isNotBlank() ||
                            it.dateTo.isNotBlank() ||
                            it.startTime.isNotBlank()
                } ||
                state.duration.isNotBlank() ||
                state.address.isNotBlank() ||
                state.sourceUrl.isNotBlank() ||
                state.description.isNotBlank() ||
                state.ageLimit.isNotBlank() ||
                state.participantNotes.isNotBlank() ||
                state.moderatorNotes.isNotBlank()
    }

    fun saveDraft(
        contentResolver: ContentResolver,
        onSuccess: () -> Unit
    ) {
        val state = _uiState.value

        if (state.isSubmitted || !hasDraftContent()) {
            onSuccess()
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            eventsRepository.saveDraftEvent(
                event = state.toEventInfo(),
                photoUri = state.photoUri,
                contentResolver = contentResolver
            )
                .onSuccess { savedEvent ->
                    _uiState.value = _uiState.value.copy(
                        eventId = savedEvent.id,
                        photoUrl = savedEvent.photoUrl,
                        photoUri = null,
                        isLoading = false
                    )

                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось сохранить черновик"
                    )
                }
        }
    }

    fun submitForModeration(
        contentResolver: ContentResolver
    ) {
        if (!validateForSubmit()) return

        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            eventsRepository.submitEventForModeration(
                event = state.toEventInfo(),
                photoUri = state.photoUri,
                contentResolver = contentResolver
            )
                .onSuccess { savedEvent ->
                    _uiState.value = _uiState.value.copy(
                        eventId = savedEvent.id,
                        photoUrl = savedEvent.photoUrl,
                        photoUri = null,
                        submittedEventId = savedEvent.id,
                        isSubmitted = true,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось отправить мероприятие на модерацию"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateForSubmit(): Boolean {
        val state = _uiState.value

        var errors = CreateEventFieldErrors()
        var hasError = false

        if (state.title.isBlank()) {
            errors = errors.copy(title = "Поле обязательно для заполнения")
            hasError = true
        }

        val validatedDates = state.dates.map { slot ->
            var updated = slot.copy(
                dateFromError = null,
                dateToError = null,
                startTimeError = null
            )

            if (!slot.dateFrom.isValidDate()) {
                updated = updated.copy(dateFromError = "Введите дату в формате дд.мм.гггг")
                hasError = true
            }

            if (slot.dateTo.isNotBlank() && !slot.dateTo.isValidDate()) {
                updated = updated.copy(dateToError = "Введите дату в формате дд.мм.гггг")
                hasError = true
            }

            if (!slot.startTime.isValidTime()) {
                updated = updated.copy(startTimeError = "Введите время в формате чч:мм")
                hasError = true
            }

            updated
        }

        if (state.duration.isNotBlank() && !state.duration.isValidTime()) {
            errors = errors.copy(duration = "Введите длительность в формате чч:мм")
            hasError = true
        }

        if (state.address.isBlank()) {
            errors = errors.copy(address = "Поле обязательно для заполнения")
            hasError = true
        }

        if (!state.isOrganizer && state.sourceUrl.isBlank()) {
            errors = errors.copy(sourceUrl = "Поле обязательно для заполнения")
            hasError = true
        }

        if (state.description.isBlank()) {
            errors = errors.copy(description = "Поле обязательно для заполнения")
            hasError = true
        }

        val age = state.ageLimit.toIntOrNull()
        if (state.ageLimit.isNotBlank() && (age == null || age !in 0..99)) {
            errors = errors.copy(ageLimit = "Введите число от 0 до 99")
            hasError = true
        }

        _uiState.value = state.copy(
            dates = validatedDates,
            errors = errors
        )

        return !hasError
    }

    private fun CreateEventUiState.toEventInfo(): EventInfo {
        return EventInfo(
            id = eventId,
            title = title.trim(),
            description = description.trim(),
            photoUrl = photoUrl,
            dates = dates.map { slot ->
                val startAt = parseStartAt(slot.dateFrom, slot.startTime)
                val endAt = parseEndAt(
                    dateFrom = slot.dateFrom,
                    dateTo = slot.dateTo,
                    startTime = slot.startTime,
                    duration = duration
                )

                EventDateSlot(
                    dateFrom = slot.dateFrom,
                    dateTo = slot.dateTo,
                    startTime = slot.startTime,
                    startAt = startAt,
                    endAt = endAt
                )
            },
            duration = duration.trim(),
            address = address.trim(),
            isOrganizer = isOrganizer,
            sourceUrl = sourceUrl.trim(),
            ageLimit = ageLimit.toIntOrNull(),
            participantNotes = participantNotes.trim(),
            moderatorNotes = moderatorNotes.trim(),
            notifyAboutModeration = notifyAboutModeration
        )
    }

    private fun parseStartAt(
        dateFrom: String,
        startTime: String
    ): Timestamp? {
        return parseDateTime(dateFrom, startTime)
    }

    private fun parseEndAt(
        dateFrom: String,
        dateTo: String,
        startTime: String,
        duration: String
    ): Timestamp? {
        val baseDate = dateTo.ifBlank { dateFrom }
        val start = parseDateTime(baseDate, startTime) ?: return null

        if (!duration.isValidTime()) {
            return start
        }

        val parts = duration.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            time = start.toDate()
            add(Calendar.HOUR_OF_DAY, hours)
            add(Calendar.MINUTE, minutes)
        }

        return Timestamp(calendar.time)
    }

    private fun parseDateTime(
        date: String,
        time: String
    ): Timestamp? {
        if (!date.isValidDate() || !time.isValidTime()) return null

        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            format.isLenient = false
            val parsed = format.parse("$date $time") ?: return null

            Timestamp(parsed)
        } catch (_: Exception) {
            null
        }
    }

    private fun String.toDateMask(): String {
        val digits = filter { it.isDigit() }.take(8)

        return buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index == 1 || index == 3) append(".")
            }
        }
    }

    private fun String.toTimeMask(): String {
        val digits = filter { it.isDigit() }.take(4)

        return buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index == 1) append(":")
            }
        }
    }

    private fun String.isValidDate(): Boolean {
        if (!matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) return false

        val parts = split(".")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val year = parts.getOrNull(2)?.toIntOrNull() ?: return false

        if (year !in 2024..2100) return false
        if (month !in 1..12) return false
        if (day !in 1..31) return false

        return true
    }

    private fun String.isValidTime(): Boolean {
        if (!matches(Regex("""\d{2}:\d{2}"""))) return false

        val parts = split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return false

        return hour in 0..23 && minute in 0..59
    }

    companion object {
        const val TITLE_MAX = 80
        const val ADDRESS_MAX = 160
        const val SOURCE_URL_MAX = 300
        const val DESCRIPTION_MAX = 1200
        const val PARTICIPANT_NOTES_MAX = 500
        const val MODERATOR_NOTES_MAX = 500
    }
}