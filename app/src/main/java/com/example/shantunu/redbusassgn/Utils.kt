package com.example.shantunu.redbusassgn

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class Utils {

    companion object {
        fun getTime(dateStr : String) : String{
//        val date = LocalDate.parse("9999-12-31")
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            return SimpleDateFormat("hh:mm aa").format(df.parse(dateStr))
        }

        fun getReachesLocationIn(minutes : Long) : String {
            val hours = minutes / 60 //since both are ints, you get an int
            val mins = minutes % 60
            if (minutes >= 60) {
                return hours.toString()+ " mins " + mins + " secs"
            }
            else {
                return mins.toString() + " seconds"
            }
        }

        fun getEndTime(duration : Long, startTime : String) : String {
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            val date = df.parse(startTime).time + TimeUnit.MINUTES.toMillis(duration)
            return SimpleDateFormat("HH:mm aa").format(date)
        }

    }

}