package com.example

import com.example.data.model.CardEvent
import com.example.data.model.CardType
import com.example.data.model.SaturdayCalendarHelper
import com.example.data.model.Student
import com.example.ui.viewmodel.KamScreenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

import com.example.data.model.Group
import com.example.data.model.ServantBlock
import com.example.ui.viewmodel.KamViewModel
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

class ExampleUnitTest {

    @Test
    fun groupDailyInviteCode_zeroCollisionAcrossGroups_and24HourExpirationAndBannedList() {
        val nowMillis = 1_762_000_000_000L
        val groupA = Group(
            id = "KAMHX3RK",
            name = "فصل مارمينا",
            inviteCode = "KAMHX3RK",
            dailyInviteToken = "482",
            inviteCodeUpdatedAtMillis = nowMillis - (2 * 60 * 60 * 1000L), // 2 hours ago
            bannedUids = listOf("uid_banned_1"),
            bannedServantEntries = listOf("uid_banned_1|أ. هاني")
        )
        // Even if groupB happens to roll the exact same daily token "482", its group prefix guarantees 0% collision
        val groupB = Group(
            id = "KAM99ZZ1",
            name = "فصل الأنبا أنطونيوس",
            inviteCode = "KAM99ZZ1",
            dailyInviteToken = "482",
            inviteCodeUpdatedAtMillis = nowMillis - (25 * 60 * 60 * 1000L) // 25 hours ago -> expired!
        )

        assertEquals("KAMHX3RK-482", groupA.fullDailyInviteCode)
        assertEquals("KAM99ZZ1-482", groupB.fullDailyInviteCode)
        assertNotEquals(groupA.fullDailyInviteCode, groupB.fullDailyInviteCode)

        // Parsing both hyphenated and non-hyphenated inputs routes to the exact unique group prefix
        assertEquals("KAMHX3RK" to "482", Group.parseInviteCodeInput(" kamhx3rk-482 "))
        assertEquals("KAMHX3RK" to "482", Group.parseInviteCodeInput("KAMHX3RK482"))
        assertEquals("KAM99ZZ1" to "482", Group.parseInviteCodeInput("KAM99ZZ1-482"))

        // 24-hour validity check
        assertFalse(groupA.isDailyCodeExpired(nowMillis))
        assertEquals(22, groupA.remainingDailyCodeHours(nowMillis))

        assertTrue(groupB.isDailyCodeExpired(nowMillis))
        assertEquals(0, groupB.remainingDailyCodeHours(nowMillis))

        // Banned servants list parsing
        assertEquals(1, groupA.bannedServantsList.size)
        assertEquals("uid_banned_1", groupA.bannedServantsList.first().uid)
        assertEquals("أ. هاني", groupA.bannedServantsList.first().name)
    }

    @Test
    fun saturdayCalendarHelper_startsFromNovember2026WithCopticMonthsWeeksAndTransitionBadge() {
        // Sunday Nov 1, 2026
        val nowCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 1, 10, 0, 0)
        }
        val tree2026 = SaturdayCalendarHelper.buildYearTree(2026, nowCal)
        // Only November (11) and December (12) exist in 2026; older months 1..10 are removed
        assertEquals(2, tree2026.months.size)
        assertEquals(11, tree2026.months[0].monthNumber)
        assertEquals(12, tree2026.months[1].monthNumber)

        val november = tree2026.months[0]
        assertTrue(november.isCurrentMonth)
        assertFalse(november.isFutureMonth)
        assertTrue(november.copticMonthsLabel.contains("بابه"))
        assertTrue(november.copticMonthsLabel.contains("هاتور"))
        assertTrue(november.weeks.isNotEmpty())

        // In November 2026: Nov 1 is Sunday .. Nov 6 is Friday (Week 1), Nov 7 is Saturday .. Nov 13 is Friday (Week 2)
        assertEquals(1, november.weeks[0].startDayOfMonth)
        assertEquals(6, november.weeks[0].endDayOfMonth)
        assertNull(november.weeks[0].copticTransitionBadge)

        // Week 2 (Nov 7..13) contains Nov 10 = 1 Hathor -> has a Coptic Month Transition Badge!
        val week2 = november.weeks[1]
        assertEquals(7, week2.startDayOfMonth)
        assertEquals(13, week2.endDayOfMonth)
        assertNotNull(week2.copticTransitionBadge)
        assertTrue(week2.copticTransitionBadge!!.contains("هاتور"))

        // December 2026 is a future month relative to Nov 1, 2026 -> locked & empty weeks
        val december = tree2026.months[1]
        assertTrue(december.isFutureMonth)
        assertTrue(december.weeks.isEmpty())
    }

    @Test
    fun computeSequentiallyNumberedStudents_recomputesGlobalOneToNOrderWhenStudentMoves() {
        val blockA = ServantBlock(id = "blk_a", servantName = "أ. مينا", sortOrder = 0)
        val blockB = ServantBlock(id = "blk_b", servantName = "أ. كيرلس", sortOrder = 1)
        val blocks = listOf(blockA, blockB)

        val s1 = Student(id = "s1", name = "أبانوب", blockId = "blk_a", position = 1000.0, studentNumber = 99)
        val s2 = Student(id = "s2", name = "بيشوي", blockId = "blk_a", position = 2000.0, studentNumber = 42)
        val s3 = Student(id = "s3", name = "جرجس", blockId = "blk_b", position = 1000.0, studentNumber = 15)
        val s4 = Student(id = "s4", name = "دانيال", blockId = "", position = 1000.0, studentNumber = 7)

        val initialOrdered = KamViewModel.computeSequentiallyNumberedStudents(
            rawStudents = listOf(s4, s3, s2, s1),
            blocks = blocks
        )
        assertEquals(listOf("s1", "s2", "s3", "s4"), initialOrdered.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), initialOrdered.map { it.studentNumber })

        // Now move s2 above s1 inside blockA (s2 position = 500.0, s1 position = 1000.0)
        val reordered = KamViewModel.computeSequentiallyNumberedStudents(
            rawStudents = listOf(
                s1.copy(position = 1000.0),
                s2.copy(position = 500.0),
                s3,
                s4
            ),
            blocks = blocks
        )
        assertEquals(listOf("s2", "s1", "s3", "s4"), reordered.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), reordered.map { it.studentNumber })
    }

    @Test
    fun duplicateAlerts_detectsSameStudentSameCardTwoDifferentServantsInSameWeek() {
        val student = Student(id = "stu_1", name = "مينا جرجس", studentNumber = 1)
        val events = listOf(
            CardEvent(
                id = "evt_1",
                studentId = "stu_1",
                studentName = "مينا جرجس",
                cardType = CardType.ATTENDANCE.id,
                points = 3,
                servantId = "srv_1",
                servantName = "أ. أبانوب",
                monthKey = "2026-10",
                weekKey = "2026-10-W5"
            ),
            CardEvent(
                id = "evt_2",
                studentId = "stu_1",
                studentName = "مينا جرجس",
                cardType = CardType.ATTENDANCE.id,
                points = 3,
                servantId = "srv_2",
                servantName = "أ. مايكل",
                monthKey = "2026-10",
                weekKey = "2026-10-W5"
            ),
            // Different card type -> should NOT trigger duplicate alert
            CardEvent(
                id = "evt_3",
                studentId = "stu_1",
                studentName = "مينا جرجس",
                cardType = CardType.QUIET.id,
                points = 5,
                servantId = "srv_1",
                servantName = "أ. أبانوب",
                monthKey = "2026-10",
                weekKey = "2026-10-W5"
            )
        )

        val state = KamScreenState(
            students = listOf(student),
            cardEvents = events,
            selectedMonthKey = "2026-10",
            selectedWeekKey = "2026-10-W5"
        )

        assertEquals(1, state.duplicateAlerts.size)
        val alert = state.duplicateAlerts.first()
        assertEquals(
            "الطفل مينا جرجس أخد كارت الحضور مرتين: أ. أبانوب وأ. مايكل",
            alert.formattedMessage
        )
    }

    @Test
    fun formatCompactHeaderMonthWeek_formatsShortMonthYearAndWeekFor56dpHeader() {
        val compactWeekLabel = SaturdayCalendarHelper.formatCompactHeaderMonthWeek(
            monthKey = "2026-10",
            selectedWeekKey = "2026-10-W5"
        )
        assertEquals("أكتوبر ٢٦ • أسبوع ٥", compactWeekLabel)

        val compactAllWeeksLabel = SaturdayCalendarHelper.formatCompactHeaderMonthWeek(
            monthKey = "2026-10",
            selectedWeekKey = null
        )
        assertEquals("أكتوبر ٢٦ • الكل", compactAllWeeksLabel)
    }

    @Test
    fun cardCountForStudent_drivesSymbolToPointsTransformationOnCardButton() {
        val student = Student(
            id = "stu_10",
            name = "كيرلس سامح",
            studentNumber = 10L,
            avatarBase64 = "base64_sample_thumb"
        )
        val initialState = KamScreenState(
            students = listOf(student),
            cardEvents = emptyList(),
            selectedMonthKey = "2026-10",
            selectedWeekKey = "2026-10-W5"
        )
        // Before tapping: count is 0 -> button displays symbol only
        assertEquals(0, initialState.cardCountForStudent("stu_10", CardType.ATTENDANCE))

        // After tapping Attendance (3 pts): count is 1 -> button transforms to points number (3)
        val recordedEvent = CardEvent(
            id = "evt_stu_10_ATTENDANCE_2026-10-W5_srv_1",
            studentId = "stu_10",
            studentName = "كيرلس سامح",
            cardType = CardType.ATTENDANCE.id,
            points = CardType.ATTENDANCE.points.toLong(),
            servantId = "srv_1",
            servantName = "أ. أبانوب",
            monthKey = "2026-10",
            weekKey = "2026-10-W5"
        )
        val awardedState = initialState.copy(cardEvents = listOf(recordedEvent))
        assertEquals(1, awardedState.cardCountForStudent("stu_10", CardType.ATTENDANCE))
        assertEquals(3, CardType.ATTENDANCE.points * awardedState.cardCountForStudent("stu_10", CardType.ATTENDANCE))
    }
}


