package com.polimi.tiw_asteonline_ria.utils;


import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;


public class Checks {
    public static boolean hasDuplicates(int[] arr) {
        return Arrays.stream(arr)
                .boxed()
                .collect(HashSet::new, (set, num) -> {
                    if (!set.add(num)) {
                        throw new IllegalStateException("Duplicate element found: " + num);
                    }
                }, HashSet::addAll)
                .size() < arr.length;
    }

    public static boolean hasNegatives(int[] arr) {
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] <= 0) return true;
        }
        return false;
    }

    public static boolean checkExpired(Timestamp deadline) {
        Date currentDate = new Date();
        Duration duration = Duration.between(currentDate.toInstant(),deadline.toInstant());
        return duration.isNegative();
    }

    public static String calculateTimeRemaining(Timestamp deadline) {
        Date currentDate = new Date();
        Duration duration = Duration.between(currentDate.toInstant(),deadline.toInstant());

        if(duration.isNegative()){
            return "Asta scaduta!";
        }

        if (duration.toHours() == 0) {
            return "Meno di 1 ora";
        }

        String hours;
        if(duration.toHoursPart() == 1){
            hours = "1 ora";
        } else {
            hours = String.format("%d ore", duration.toHoursPart());
        }

        if(duration.toDays() == 0){
            return hours;
        }

        String days;
        if(duration.toDays() == 1){
            days = "1 giorno";
        } else {
            days = String.format("%d giorni", duration.toDays());
        }

        if(duration.toHoursPart() == 0){
            return days;
        } else {
            return days + " e " + hours;
        }
    }


}
