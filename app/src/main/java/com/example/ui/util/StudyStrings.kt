package com.example.ui.util

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    BENGALI("bn", "বাংলা (Mixed)", "🇧🇩"),
    ENGLISH("en", "English", "🇬🇧")
}

object StudyStrings {

    fun getAppName(lang: AppLanguage): String = "OmniStudy AI"

    fun getDashboardTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Home" else "Home"
    fun getHubTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Study Hub" else "Hub"
    fun getCardsTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Flashcards" else "Cards"
    fun getExamsTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Mock Exams" else "Exams"
    fun getFocusTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Deep Focus" else "Focus"
    fun getFeynmanTab(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) "Feynman AI" else "Feynman"

    fun getWelcomeGreeting(name: String, lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Welcome back, $name!" else "Welcome back, $name"

    fun getDailyGoal(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Daily Study Target" else "Daily Focus Goal"

    fun getStreakLabel(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Study Streak" else "Streak"

    fun getXpLabel(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Total XP" else "XP Total"

    fun getDueCardsLabel(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Due Cards" else "Due Cards"

    fun getInterleavedTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "Interleaved Study Routine" else "Interleaved Study Timetable"

    fun getInterleavedSubtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "একটানা এক Subject না পড়ে ঘুরিয়ে-ফিরিয়ে Study করলে পড়া সহজে Recall করা যায় আর Long-term মনে থাকে।"
        else "Mixing related subjects rather than blocking single subjects maximizes neural consolidation and retention."

    fun getFreeTierNotice(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "100% Free • কোনো Subscription বা Hidden Charge নেই" else "100% Free Tier • Zero Cost"

    // Onboarding strings
    fun getOnboardingSkip(lang: AppLanguage): String = "Skip"

    fun getOnboardingNext(lang: AppLanguage): String = "Next"

    fun getOnboardingBack(lang: AppLanguage): String = "Back"

    fun getOnboardingGetStarted(lang: AppLanguage): String = "Get Started"
}
