package com.example.myapplication.utils

import java.util.Locale

object TaskCategoryHelper {

    fun getCategoryIcon(title: String): String {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("study") || lower.contains("class") || lower.contains("homework") || lower.contains("exam") -> "📚"
            lower.contains("assignment") || lower.contains("cs") || lower.contains("code") || lower.contains("program") -> "</>"
            lower.contains("walk") || lower.contains("run") || lower.contains("jog") || lower.contains("step") -> "👟"
            lower.contains("read") || lower.contains("sapiens") || lower.contains("book") || lower.contains("novel") -> "📖"
            lower.contains("feedback") || lower.contains("client") || lower.contains("review") || lower.contains("chat") -> "💬"
            lower.contains("meditate") || lower.contains("relax") || lower.contains("yoga") || lower.contains("mind") -> "🪷"
            lower.contains("electric") || lower.contains("bill") || lower.contains("pay") || lower.contains("light") -> "🔌"
            lower.contains("gym") || lower.contains("workout") || lower.contains("exercise") || lower.contains("fit") -> "💪"
            lower.contains("water") || lower.contains("drink") || lower.contains("hydrate") -> "💧"
            lower.contains("meet") || lower.contains("meeting") || lower.contains("office") || lower.contains("work") -> "💼"
            lower.contains("shop") || lower.contains("buy") || lower.contains("grocer") || lower.contains("market") -> "🛒"
            lower.contains("meds") || lower.contains("medicine") || lower.contains("pill") || lower.contains("doctor") -> "💊"
            lower.contains("sleep") || lower.contains("bed") || lower.contains("nap") || lower.contains("rest") -> "😴"
            else -> "⏰"
        }
    }
}
