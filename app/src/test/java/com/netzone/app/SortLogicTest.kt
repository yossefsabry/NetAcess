package com.netzone.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SortLogicTest {

    @Test
    fun testSmartSortPriorities() {
        // Sample data
        val apps = listOf(
            AppMetadata("com.a", "App A", 1, false),
            AppMetadata("com.b", "App B", 2, false),
            AppMetadata("com.c", "App C", 3, false),
            AppMetadata("com.d", "App D", 4, false),
            AppMetadata("com.e", "App E", 5, false)
        )

        // Rules: App B and App D are blocked
        val rulesMap = mapOf(
            "com.b" to Rule("com.b", "App B", 2, wifiBlocked = true),
            "com.d" to Rule("com.d", "App D", 4, mobileBlocked = true)
        )

        // Recent: App C and App E are recent. App B (blocked) is also recent.
        val recentPackages = listOf("com.c", "com.e", "com.b")

        // Priorities: Blocked > Recently Active > Alphabetical
        // Expected order:
        // 1. App B (Blocked, Recent)
        // 2. App D (Blocked, Not Recent)
        // 3. App C (Not Blocked, Recent, 'C' < 'E')
        // 4. App E (Not Blocked, Recent, 'E' > 'C')
        // 5. App A (Not Blocked, Not Recent)

        val sorted = MainViewModel.sortSmart(apps, rulesMap, recentPackages)

        assertEquals("com.b", sorted[0].packageName)
        assertEquals("com.d", sorted[1].packageName)
        assertEquals("com.c", sorted[2].packageName)
        assertEquals("com.e", sorted[3].packageName)
        assertEquals("com.a", sorted[4].packageName)
    }

    @Test
    fun testSmartSortBlockedBySchedule() {
        val apps = listOf(
            AppMetadata("com.a", "App A", 1, false),
            AppMetadata("com.b", "App B", 2, false)
        )
        // App B is blocked by schedule
        val rulesMap = mapOf(
            "com.b" to Rule("com.b", "App B", 2, isScheduleEnabled = true)
        )
        val recentPackages = emptyList<String>()

        val sorted = MainViewModel.sortSmart(apps, rulesMap, recentPackages)

        assertEquals("com.b", sorted[0].packageName)
        assertEquals("com.a", sorted[1].packageName)
    }

    @Test
    fun testSmartSortEmptyList() {
        val apps = emptyList<AppMetadata>()
        val rulesMap = emptyMap<String, Rule>()
        val recentPackages = emptyList<String>()

        val sorted = MainViewModel.sortSmart(apps, rulesMap, recentPackages)
        assert(sorted.isEmpty())
    }

    @Test
    fun testSmartSortNoRules() {
        val apps = listOf(
            AppMetadata("com.b", "App B", 2, false),
            AppMetadata("com.a", "App A", 1, false)
        )
        val rulesMap = emptyMap<String, Rule>()
        val recentPackages = emptyList<String>()

        val sorted = MainViewModel.sortSmart(apps, rulesMap, recentPackages)

        // Should fall back to alphabetical
        assertEquals("com.a", sorted[0].packageName)
        assertEquals("com.b", sorted[1].packageName)
    }
}
