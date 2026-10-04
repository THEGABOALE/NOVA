package com.mindflow.nova.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mindflow.nova.ui.components.stackStats
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentStreak
import com.mindflow.nova.ui.components.NovaProgressBar
import com.mindflow.nova.ui.components.StreakBadge
import com.mindflow.nova.ui.components.streakMessage
import com.mindflow.nova.ui.screens.home.levelProgressPercentage
import com.mindflow.nova.ui.screens.lessons.MissionState
import com.mindflow.nova.ui.screens.lessons.computeMissionStates
import com.mindflow.nova.ui.screens.lessons.currentMissionIndex
import com.mindflow.nova.ui.theme.NovaBlue
import com.mindflow.nova.ui.theme.NovaInfoBackground
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaNeutralCard
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

// Progreso cuenta cómo va la ruta del nivel: la racha, cuánto lleva y en qué
// estado está cada misión. Las cifras (misiones y semillas) quedan en el
// Perfil, para no repetirlas. Los logros (insignias por rachas, niveles o
// repasos) van acá más adelante.
@Composable
fun ProgressScreen(
    level: LevelResponse,
    // Null mientras carga o si no se pudo obtener: se muestra como si no hubiera avance.
    progress: StudentProgress?,
    modifier: Modifier = Modifier
) {
    val percent = levelProgressPercentage(level, progress).toInt().coerceIn(0, 100)
    val missionStates = computeMissionStates(level.missions, progress?.completedMissionIds?.toSet().orEmpty())
    val currentIndex = currentMissionIndex(missionStates)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Progreso",
            color = NovaPurple,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Tu avance en ${level.name}",
            color = NovaTextSecondary,
            fontSize = 14.sp
        )

        progress?.streak?.let { streak ->
            Spacer(modifier = Modifier.height(18.dp))
            StreakCard(streak)
        }

        Spacer(modifier = Modifier.height(18.dp))

        LevelCard(levelName = level.name, percent = percent)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Misiones del nivel",
            color = NovaText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            level.missions.forEachIndexed { index, mission ->
                MissionStatusRow(
                    mission = mission,
                    state = missionStates[index],
                    isCurrent = index == currentIndex
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StreakCard(streak: StudentStreak) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = NovaSurface,
        shadowElevation = 2.dp
    ) {
        // El marcador ya dice los días; al lado va qué hacer hoy, con el mismo
        // texto que explica la racha en el Inicio. Con la fuente grande van uno
        // debajo del otro para que el texto no quede en una columna angosta.
        val message = @Composable { textModifier: Modifier ->
            Text(
                text = streakMessage(streak).body,
                modifier = textModifier,
                color = NovaText,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
        }

        if (stackStats(LocalDensity.current.fontScale)) {
            Column(modifier = Modifier.padding(16.dp)) {
                StreakBadge(streak = streak)
                Spacer(modifier = Modifier.height(12.dp))
                message(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakBadge(streak = streak)
                Spacer(modifier = Modifier.width(14.dp))
                message(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LevelCard(levelName: String, percent: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = NovaSurface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = levelName,
                color = NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = progressHeadline(percent),
                color = NovaTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            NovaProgressBar(progress = percent / 100f)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$percent % completado",
                color = NovaPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MissionStatusRow(mission: MissionResponse, state: MissionState, isCurrent: Boolean) {
    // El estado va con ícono y con texto: el color nunca es la única señal.
    val (icon, label, tint, tileColor) = when {
        state.isCompleted -> MissionStatusStyle(Icons.Rounded.Check, "Completada", NovaPurple, NovaLightPurple)
        // Con el desbloqueo en orden, la única desbloqueada sin completar es la actual.
        state.isUnlocked -> MissionStatusStyle(Icons.Rounded.PlayArrow, "Disponible", NovaBlue, NovaInfoBackground)
        else -> MissionStatusStyle(Icons.Rounded.Lock, "Bloqueada", NovaTextSecondary, NovaNeutralCard)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = NovaSurface,
        shadowElevation = if (isCurrent) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    color = if (state.isUnlocked) NovaText else NovaTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = label,
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private data class MissionStatusStyle(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
    val tileColor: Color
)
