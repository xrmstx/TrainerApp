package com.example.trainerapp.data.planshare

import com.example.trainerapp.data.model.Client
import com.example.trainerapp.data.model.Macrocycle

object PlanShareText {

    /** Весь макроцикл: заголовок с клиентом + все недели */
    fun macrocycle(client: Client, macro: Macrocycle): String =
        buildString {
            appendLine("План — ${client.name}")
            appendLine("${macro.title} (${macro.weeks.size} нед.)")
            macro.weeks.forEachIndexed { i, week ->
                appendLine()
                appendLine("── Неделя ${i + 1} ──")
                appendLine(week(week))
            }
        }.trimEnd()

    /** Одна неделя: дни без заголовка цикла */
    fun week(week: Week): String =
        week.days.joinToString("\n\n") { day(it) }.trim()

    /** Один день: заголовок дня + упражнения */
    fun day(day: Day): String =
        buildString {
            appendLine("День ${day.order}")
            day.blocks.forEachIndexed { i, block ->
                val weight = block.weightKg?.let { " @ ${fmt(it)} кг" } ?: ""
                val protocol = block.protocolValue.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                appendLine("${i + 1}. ${block.exerciseName} — ${block.sets}×${block.reps}$weight$protocol")
            }
        }.trimEnd()

    private fun fmt(v: Float): String =
        if (v % 1f == 0f) v.toInt().toString()
        else v.toString().trimEnd('0').trimEnd('.')
}
