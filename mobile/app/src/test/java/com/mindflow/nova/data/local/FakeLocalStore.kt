package com.mindflow.nova.data.local

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import com.mindflow.nova.data.offline.RejectedNotice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [LocalStore] en memoria para los tests: los observadores emiten al cambiar algo. */
class FakeLocalStore(private val now: () -> Long = { 0L }) : LocalStore {

    private val users = MutableStateFlow<Map<Int, SessionUser>>(emptyMap())
    private val levels = MutableStateFlow<Map<Int, List<LevelResponse>>>(emptyMap())
    private val missions = MutableStateFlow<Map<Pair<Int, Int>, Pair<MissionContent, Long>>>(emptyMap())
    private val active = mutableMapOf<Pair<Int, String>, MissionContent>()
    private val progress = MutableStateFlow<Map<Int, StudentProgress>>(emptyMap())
    private val pending = MutableStateFlow<Map<String, PendingAttempt>>(emptyMap())
    /** Avisos por clientAttemptId, con el userId de su dueño. */
    private val notices = MutableStateFlow<Map<String, Pair<Int, RejectedNotice>>>(emptyMap())

    val clearedAccounts = mutableListOf<Int>()

    fun pendingNow(userId: Int): List<PendingAttempt> =
        pending.value.values.filter { it.userId == userId }.sortedBy { it.finishedAt }

    override suspend fun saveUser(user: SessionUser) = users.update { it + (user.id to user) }
    override suspend fun user(userId: Int): SessionUser? = users.value[userId]

    override suspend fun saveLevels(userId: Int, levels: List<LevelResponse>) =
        this.levels.update { it + (userId to levels) }
    override suspend fun levels(userId: Int): List<LevelResponse>? = levels.value[userId]

    override suspend fun saveMission(userId: Int, content: MissionContent) =
        missions.update { it + ((userId to content.id) to (content to now())) }
    override suspend fun mission(userId: Int, missionId: Int): MissionContent? = missions.value[userId to missionId]?.first
    override suspend fun missionSavedAt(userId: Int, missionId: Int): Long? = missions.value[userId to missionId]?.second

    override suspend fun saveActiveLesson(userId: Int, attemptId: String, content: MissionContent) { active[userId to attemptId] = content }
    override suspend fun activeLesson(userId: Int, attemptId: String) = active[userId to attemptId]
    override suspend fun clearActiveLessons(userId: Int) { active.keys.removeAll { it.first == userId } }

    override suspend fun saveProgress(userId: Int, progress: StudentProgress) =
        this.progress.update { it + (userId to progress) }
    override suspend fun progress(userId: Int): StudentProgress? = progress.value[userId]
    override fun observeProgress(userId: Int): Flow<StudentProgress?> = progress.map { it[userId] }

    override suspend fun addPending(attempt: PendingAttempt) =
        pending.update { it + (attempt.clientAttemptId to attempt) }
    override suspend fun pending(userId: Int): List<PendingAttempt> = pendingNow(userId)
    override fun observePending(userId: Int): Flow<List<PendingAttempt>> =
        pending.map { all -> all.values.filter { it.userId == userId }.sortedBy { it.finishedAt } }
    override suspend fun settlePending(userId: Int, doneIds: List<String>, notices: List<RejectedNotice>) {
        this.notices.update { all -> all + notices.associate { it.clientAttemptId to (userId to it) } }
        pending.update { it - doneIds.toSet() }
    }
    override fun observeNotices(userId: Int): Flow<List<RejectedNotice>> =
        notices.map { all -> all.values.filter { it.first == userId }.map { it.second } }
    override suspend fun clearNotices(userId: Int) = notices.update { all -> all.filterValues { it.first != userId } }
    override suspend fun removeNotice(clientAttemptId: String) = notices.update { it - clientAttemptId }

    override suspend fun clearAccount(userId: Int) {
        clearedAccounts += userId
        users.update { it - userId }
        levels.update { it - userId }
        progress.update { it - userId }
        missions.update { all -> all.filterKeys { it.first != userId } }
        clearActiveLessons(userId)
    }
}
