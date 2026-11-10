package com.mindflow.nova.data.model

data class TeacherRoomsResponse(val message: String?, val status: String?, val rooms: List<TeacherRoom>)

data class TeacherRoom(
    val id: Int, val name: String, val grade: String, val section: String?,
    val schoolYear: Int, val levelId: Int, val studentCount: Int,
    val students: List<TeacherStudent>, val resultsByMechanic: List<TeacherMechanicResult>
)

data class TeacherStudent(
    val id: Int, val fullName: String, val totalPoints: Int, val missionsCompleted: Int,
    val lastAttemptAt: String?, val lastAttemptSeconds: Int?
)

data class TeacherMechanicResult(val mechanic: String, val averageScore: Double)
