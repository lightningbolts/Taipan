package taipan

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Location {
    HongKong, Shanghai, Nagasaki, Saigon, Manila, Singapore, Batavia
}

enum class Commodity {
    Opium, Silk, Arms, General;

    companion object {
        fun fromAbbreviation(input: String): Commodity =
            when (input) {
                "o" -> Opium
                "s" -> Silk
                "a" -> Arms
                "g" -> General
                else -> throw UnknownAbbreviationException("")
            }
    }

    class UnknownAbbreviationException (message: String): Exception(message)
}

object Ship {
    var cannons = 5
    var health = 100
    var cargoUnits = 150
    var hold = 100
    val commodities = mutableMapOf(
        Commodity.Opium to 0,
        Commodity.Silk to 0,
        Commodity.Arms to 0,
        Commodity.General to 0
    )
    var moneyInBank = 0
    var cash = 500
    var debt = 5000
    var location = Location.HongKong
}

object Prices {
    var commodities = mutableMapOf(
        Commodity.Opium to 3,
        Commodity.Silk to 0,
        Commodity.Arms to 0,
        Commodity.General to 0
    )
    var isRandom: Boolean = false
}

object Warehouse {
    var commodities: MutableMap<Commodity, Int> = mutableMapOf(
        Commodity.Opium to 0,
        Commodity.Silk to 0,
        Commodity.Arms to 0,
        Commodity.General to 0
    )
    var vacantCargoSpaces = 10000
    const val totalCargoSpaces = 10000

    val occupiedCargoSpaces: Int
        get() = totalCargoSpaces - vacantCargoSpaces
}

object LiYuen {
    var chanceOfAttack = 0.5
    var chanceOfExtortion = 0.8
    var extortionMultiplier = 1.0
}

val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
var month = 0
var year = 1860
var chanceOfSeaEvent = 0.5
var chanceOfPortEvent = 0.25
var isRunning = true

// Originally gameAttributes.monthLabel
val monthName: String
    get() = monthNames[month]

// Originally time()
val globalMultiplier: Double
    get() = 1.0 + month / 10000

fun input(prompt: String): String {
    print("$prompt ")
    return readLine()!!
}

/**
 * Repeatedly takes user input until [handler] returns false. That is, [handler] should return whether to continue looping.
 */
fun inputLoop(prompt: String, handler: (String) -> Boolean) {
    while (true) {
        if (!handler(input(prompt))) break
    }
}

/**
 * Repeatedly takes user input until the user types an integer AND [handler] returns false.
 */
fun intInputLoop(prompt: String, handler: (Int) -> Boolean) {
    inputLoop (prompt) {
        try {
            handler(it.toInt())
        } catch (_: NumberFormatException) {
            true
        }
    }
}

fun priceGenerator(max: Int): Int =
    (
        (5..25)
            .random()
            .toDouble()
        * max.toDouble()
        * globalMultiplier
    ).roundToInt()

fun pirateGenerator(min: Int, max: Int): Int =
    (
        (min..max)
            .random()
            .toDouble()
        * globalMultiplier
    ).roundToInt()

fun randomPriceGenerator() {

}

/**
 * Asks the user what and how much they would like to buy ([buying] = true) or sell ([buying] = false).
 */
fun exchangeHandler(buying: Boolean) {
    val actionString = if (buying) "buy" else "sell"

    inputLoop ("What do you wish to $actionString, Taipan? ") whichCommodity@{ commodity ->
        try {
            val product = Commodity.fromAbbreviation(commodity)
            val priceOfProduct = Prices.commodities[product]!!
            val directionMultiplier = if(buying) +1 else -1
            val numberOfProductsAffordable = Ship.cash / priceOfProduct

            intInputLoop ("How many units of ${product.name} do you want to $actionString? ${(if(buying) "You can afford $numberOfProductsAffordable" else "") + "."}") {
                // `it` is the number of products to buy/sell

                if (
                    // Buying more than the player can afford
                    (buying && it > numberOfProductsAffordable)
                    // Or selling more than the player has
                    || (!buying && it > Ship.commodities[product]!!)
                ) {
                    println(
                        if (buying) "You can't afford that!"
                        else "You don't have that much ${product.name}!"
                    )
                    true
                } else if (it >= 0) {
                    Ship.commodities[product] = Ship.commodities[product]!! + directionMultiplier * it
                    Ship.cash -= directionMultiplier * it * priceOfProduct
                    Ship.hold -= directionMultiplier * it
                    false
                } else true
            }

            return@whichCommodity false
        } catch (_: Commodity.UnknownAbbreviationException) {}

        true
    }
}

/**
 * TODO transferCargoHandler()
 */
fun transferCargoHandler(product: Commodity, toWarehouse: Boolean) {
    val directionMultiplier = if(toWarehouse) +1 else -1
    val actionString = if(toWarehouse) "to the warehouse" else "aboard ship"

    if (Ship.commodities[product]!! > 0) {
        intInputLoop ("How much ${product.name} shall I move $actionString, Taipan?") {
            true
        }
    }
}

fun pirates(type: String, number: Int) {

}

fun main() {
    println("Welcome to Taipan!")

    // This is the main loop, each iteration of which is a different port.
    while (isRunning) {
        // The shipyard and moneylender only bother you if you're in Hong Kong.
        if (Ship.location == Location.HongKong) {
            // If low on health, go to the shipyard.
            if (Ship.health < 100) {
                val shipIstTotScalar: Double = 1 + (1 - (100 - Ship.health) / 100.0)
                val shipFixPrice: Int =
                    (
                        Random.nextInt(1, Ship.cargoUnits)
                        * shipIstTotScalar
                        * globalMultiplier
                        * (1..5).random()
                    ).roundToInt()

                println("Captain McHenry of the Hong Kong Consolidated Repair Corporation walks over to your ship and says: <<")
                println(
                    if (Ship.health < 30)
                        "Matey! That ship of yours is 'bout to rot away like a piece of driftwood in Kolwoon Bay! Don't worry, it's nothing I can't fix. For a price, that is!"
                    else if (Ship.health < 50)
                        "That there ship's taken quite a bit of damage, matey! You best get it fixed before you go out to sea again! I can get you sailing the friendly waves in no time! For a price, that is!"
                    else
                        "What a mighty fine ship you have there, matey! Or, shall I say, had... It could really use some of what I call \"Tender Love n' Care\". 'Tis but a scratch, as they say, but I take any job, no matter how small. For a price, that is!"
                )
                println("I'll fix you up to full workin' order for $shipFixPrice pound sterling>>")
                println("Taipan, how much will you pay Captain McHenry? You have ${Ship.cash} pound sterling on hand.")

                intInputLoop (">> ") payCaptain@{ amountPaid ->

                    if (amountPaid > Ship.cash) {
                        println("Taipan, you only have ${Ship.cash} cash.")
                        true
                    } else if (amountPaid > shipFixPrice) {
                        // If the player paid what the captain asked for, completely repair the ship
                        Ship.health = 100
                        Ship.cash -= amountPaid
                        false
                    } else if (amountPaid >= 0) {
                        // If the player pays x% of what was asked, repair x% of the damaged ship
                        Ship.health += (100 - Ship.health) * amountPaid / shipFixPrice
                        Ship.cash -= amountPaid
                        false
                    } else true
                }
            }

            if (Random.nextDouble() <= LiYuen.chanceOfExtortion) {
                // TODO LiYuen
            }

            LiYuen.chanceOfExtortion += 0.01

            /* Money lender */

            inputLoop ("Do you have business with Elder Brother Wu, the moneylender?") {
                when (it) {
                    "y" -> {
                        /**
                         * TODO Money lender logic
                         * The original JS code contains a while(true) loop that never exits under some circumstances.
                         */

                        false
                    }
                    "n" -> false
                    else -> true
                }
            }
        }

        if (Random.nextDouble() <= chanceOfPortEvent) {
            // TODO Port event
        }

        Prices.isRandom = false

        if (Random.nextDouble() <= 0.1) {
            // TODO Random price
            Prices.isRandom = true
            // TODO Random price display
        } else {
            // TODO Price display
        }

        // Main loop
        while (true) {
            // Display all known information.
            println("Player---------------------------Player")
            println("Bank: ${Ship.moneyInBank}")
            println("Cash: ${Ship.cash}")
            println("Debt: ${Ship.debt}")
            println("Location: ${Ship.location}")
            println("Date: $monthName of $year")
            println("Ship---------------------------Ship")
            println("Cannons: ${Ship.cannons}")
            println("Health: ${Ship.health}")
            println("Units: ${Ship.cargoUnits}")
            println("Hold: ${Ship.hold}")
            for (commodity in Commodity.values()) {
                println("${commodity.name}: ${Ship.commodities[commodity]}")
            }
            println("Warehouse---------------------------Warehouse")
            for (commodity in Commodity.values()) {
                println("${commodity.name}: ${Warehouse.commodities[commodity]}")
            }
            println("In Use: ${Warehouse.occupiedCargoSpaces}")
            println("Vacant: ${Warehouse.vacantCargoSpaces}")
            println("Prices-----------------------------Prices")
            println("Taipan, prices per unit here are:")
            for (commodity in Commodity.values()) {
                println("${commodity.name}: ${Prices.commodities[commodity]}")
            }

            /*
            Prompt the user.
             */

            val inHongKong = Ship.location == Location.HongKong

            when (input("Shall I Buy, Sell, Visit Bank, Transfer Cargo, Quit Trading, or Retire? ")) {
                "b" -> exchangeHandler(buying = true)
                "s" -> exchangeHandler(buying = false)
                "v" -> if (inHongKong) {
                    intInputLoop ("How much will you deposit?") { cashToDeposit ->
                        if (cashToDeposit > Ship.cash) {
                            println("Taipan, you only have ${Ship.cash} in your wallet.")
                            true
                        } else if (cashToDeposit >= 0) {
                            Ship.cash -= cashToDeposit
                            Ship.moneyInBank += cashToDeposit
                            false
                        } else false
                    }
                    intInputLoop ("How much will you withdraw?") { cashToWithdraw ->
                        if (cashToWithdraw > Ship.moneyInBank) {
                            println("Taipan, you only have ${Ship.moneyInBank} in your bank.")
                            true
                        } else if (cashToWithdraw >= 0) {
                            Ship.cash += cashToWithdraw
                            Ship.moneyInBank -= cashToWithdraw
                            false
                        } else false
                    }
                }
                "t" -> if (inHongKong) {
                    // TODO Transfer cargo
                }
                "q" -> if (Ship.hold < 0) {
                    println("Your ship would be overburdened, Taipan!")
                } else {
                    // TODO Quit trading
                    break
                }
                "r" -> if (inHongKong && Ship.moneyInBank + Ship.cash >= 1000000) {
                    // TODO Retire
                    break
                }
            }
        }

        if (!isRunning) break

        // TODO Sea event
        val rnd = Random.nextDouble()

        println("Arriving at ${Ship.location}")
        ++month
        if (month == 0) ++year
        Ship.debt = (Ship.debt * 1.2) as Int
        Ship.moneyInBank = (Ship.moneyInBank * 1.05) as Int
    }

    println("Game terminated.")
}