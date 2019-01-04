package com.example.shantunu.redbusassgn

import com.example.shantunu.redbusassgn.apiModels.EachRange
import com.example.shantunu.redbusassgn.apiModels.Inventory
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class Utils {

    companion object {
        fun getTime(dateStr: String): String {
//        val date = LocalDate.parse("9999-12-31")
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            return SimpleDateFormat("hh:mm aa").format(df.parse(dateStr))
        }

        fun getReachesLocationIn(minutes: Long): String {
            val hours = minutes / 60
            val mins = minutes % 60
            if (minutes >= 60) {
                return hours.toString() + " mins " + mins + " secs"
            } else {
                return mins.toString() + " seconds"
            }
        }

        fun getEndTime(duration: Long, startTime: String): String {
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            val date = df.parse(startTime).time + TimeUnit.MINUTES.toMillis(duration)
            return SimpleDateFormat("hh:mm aa").format(date)
        }

        fun getFareRange(farePostion: Int): MutableList<EachRange> {
            val fareRange = mutableListOf<EachRange>()
            when(farePostion) {
                0 -> {
                    fareRange.add(EachRange("None", true))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                1 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", true))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                2 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", true))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                3 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", true))
                }
            }
            return fareRange
        }

        fun getDurationRange(durationPosition: Int): MutableList<EachRange> {
            val durationRange = mutableListOf<EachRange>()
            when(durationPosition) {
                0 -> {
                    durationRange.add(EachRange("None", true))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", false))
                }
                1 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", true))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", false))
                }
                2 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", true))
                    durationRange.add(EachRange("10 - 15", false))
                }
                3 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", true))
                }
            }
            return durationRange
        }

        fun getFullFilteredInventory(farePostion: Int, durationPosition: Int, searchResultsCopy: MutableList<Inventory>
        ): MutableList<Inventory> {
            val inventory = mutableListOf<Inventory>()
            for (inv in searchResultsCopy) {
                if (farePostion == 0 && durationPosition == 0){
                    inventory.addAll(searchResultsCopy)
                    return  inventory
                }
                when(farePostion){
                    0->{
                        inventory.add(inv)
                    }
                    1->{
                        inv.seats?.baseFare?.let { if (it in 0..500) {
                            inventory.add(inv)
                        } }
                    }
                    2->{
                        inv.duration?.let { if (it in 500..1000) {
                            inventory.add(inv)
                        } }
                    }
                    3->{
                        inv.duration?.let { if (it in 1000..1500) {
                            inventory.add(inv)
                        } }
                    }
                }
            }
            inventory.addAll(filterByDuration(durationPosition, searchResultsCopy, inventory))
            removeExtraInvalidInventory(inventory, farePostion)
            return inventory
        }

        private fun removeExtraInvalidInventory(inventory: MutableList<Inventory>, farePostion: Int) : MutableList<Inventory> {
            val inventoryTobeRemoved = mutableListOf<Inventory>()
            for (inv in inventory) {
                when(farePostion){
                    1->{
                        inv.seats?.baseFare?.let { if (it !in 0..500) {
                            if (inventory.contains(inv))
                                inventoryTobeRemoved.add(inv)
                        } }
                    }
                    2->{
                        inv.duration?.let { if (it !in 500..1000) {
                            if (inventory.contains(inv))
                                inventoryTobeRemoved.add(inv)
                        } }
                    }
                    3->{
                        inv.duration?.let { if (it !in 1000..1500) {
                            if (inventory.contains(inv))
                                inventoryTobeRemoved.add(inv)
                        } }
                    }
                }
            }

            for (removalObject in inventoryTobeRemoved) {
                inventory.remove(removalObject)
            }

            return inventory
        }

        fun filterByDuration(durationPosition: Int, searchResultsCopy: MutableList<Inventory>, filteredByFare :MutableList<Inventory>)
                : MutableList<Inventory>{
            val inventory = mutableListOf<Inventory>()
            for (inv in searchResultsCopy){
                when(durationPosition){
                    0->{
                        inventory.add(inv)
                    }
                    1->{
                        inv.duration?.let {
                            if (it in 0..300) {
                                if (!filteredByFare.contains(inv))
                                    inventory.add(inv)
                            } else {
                                if (filteredByFare.contains(inv))
                                    filteredByFare.remove(inv)
                            }
                        }}
                    2->{
                        inv.duration?.let {
                            if (it in 300..600) {
                                if (!filteredByFare.contains(inv))
                                    inventory.add(inv)
                            } else {
                                if (filteredByFare.contains(inv))
                                    filteredByFare.remove(inv)
                            }
                        }
                    }
                    3->{
                        inv.duration?.let {
                            if (it in 600..900) {
                                if (!filteredByFare.contains(inv))
                                    inventory.add(inv)
                            } else {
                                if (filteredByFare.contains(inv))
                                    filteredByFare.remove(inv)
                            }
                        }
                    }
                }
            }
            return inventory
        }

    }
}