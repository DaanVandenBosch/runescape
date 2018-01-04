import Boss.*
import java.util.*
import kotlin.system.measureNanoTime

enum class Boss(val bossName: String, var points: Int) {
    ARAXXI("Araxxi", 20),
    BARROWS_BROTHERS("Barrows Brothers", 7),
    BARROWS_RISE_OF_THE_SIX("Barrows: Rise of the Six", 20),
    CHAOS_ELEMENTAL("Chaos Elemental", 10),
    COMMANDER_ZILYANA("Commander Zilyana", 12),
    CORPOREAL_BEAST("Corporeal Beast", 12),
    DAGANNOTH_KINGS("Dagannoth Kings", 10),
    GENERAL_GRAARDOR("General Graardor", 12),
    GIANT_MOLE("Giant Mole", 7),
    GREGOROVIC("Gregorovic", 18),
    HAR_AKEN("Har'Aken", 15),
    HELWYR("Helwyr", 18),
    KALPHITE_KING("Kalphite King", 15),
    KALPHITE_QUEEN("Kalphite Queen", 10),
    KING_BLACK_DRAGON("King Black Dragon", 7),
    KREE_ARRA("Kree'Arra", 12),
    K_RIL_TSUTSAROTH("K'ril Tsutsaroth", 12),
    LEGIONES("Legiones", 15),
    THE_MAGISTER("The Magister", 15),
    NEX("Nex", 17),
    NEX_ANGEL_OF_DEATH("Nex: Angel of Death", 25),
    QUEEN_BLACK_DRAGON("Queen Black Dragon", 10),
    TELOS("Telos", 23),
    TWIN_FURIES("Twin Furies", 18),
    TZTOK_JAD("TzTok-Jad", 10),
    VINDICTA_AND_GORVEK("Vindicta & Gorvek", 18),
    VORAGO("Vorago", 20)
}

val bossCount = values().size
val emptyBossSet: EnumSet<Boss> = EnumSet.noneOf(Boss::class.java)
val allBossSet: EnumSet<Boss> = EnumSet.allOf(Boss::class.java)
val bonusPoints = (4 * 10 + 50) / 50.0

fun averagePointsPerTask(skip: EnumSet<Boss>, extend: Boolean, group: Boolean): Double {
    val doBosses = EnumSet.complementOf(skip)

    fun recurse(roll: Int): Double {
        val num = when (roll) {
            1, 2 -> 4
            3 -> 3
            4 -> 2
            5 -> 1
            else -> return 0.0
        }

        var doTasksTotalPoints = 0.0

        for (boss in doBosses) {
            var pts = boss.points
            if (extend) pts += boss.points / 4
            if (group) pts += boss.points / 4
            doTasksTotalPoints += (pts * num / 4) + bonusPoints
        }

        val skipTasksAvgPoints = recurse(roll + 1)

        return (doTasksTotalPoints + skipTasksAvgPoints * skip.size) / bossCount
    }

    return recurse(1)
}

fun optimalAveragePointsPerTask(skip: EnumSet<Boss>,
                                test: EnumSet<Boss>,
                                extend: Boolean,
                                group: Boolean): Pair<Double, EnumSet<Boss>> {
    val doBosses = test.clone()
    doBosses.removeAll(skip)

    if (doBosses.isEmpty()) {
        return Pair(0.0, EnumSet.complementOf(skip))
    } else {
        val max = averagePointsPerTask(skip, extend, group)
        val testRecurse = test.clone()

        val maxRecurse = doBosses.map { boss ->
            val skipRecurse = skip.clone()
            skipRecurse.add(boss)
            val opt = optimalAveragePointsPerTask(skipRecurse, testRecurse, extend, group)
            testRecurse.remove(boss)
            opt
        }.maxBy { it.first }!!

        return if (max > maxRecurse.first) {
            Pair(max, EnumSet.complementOf(skip))
        } else {
            maxRecurse
        }
    }
}

fun main(args: Array<String>) {
    val time = measureNanoTime {
        val skipNone = averagePointsPerTask(emptyBossSet, true, true)
        println("Average points per task, w/o skipping: %.2f".format(skipNone))
        println()

        val (optimalPoints, optimalBossSet) = optimalAveragePointsPerTask(emptyBossSet, allBossSet, true, true)
        println("Optimal boss set (%.2f points per task on average):".format(optimalPoints))
        println(optimalBossSet.joinToString("\n") { it.bossName })
        println()
        println("Optimal skip set:")
        val optimalSkipSet = EnumSet.complementOf(optimalBossSet)
        println(optimalSkipSet.joinToString("\n") { it.bossName })
        println()
    }

    println("Done in ${time / 1_000_000}ms.")
}
