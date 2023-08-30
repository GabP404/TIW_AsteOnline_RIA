package com.polimi.tiw_asteonline_ria.utils;


import com.polimi.tiw_asteonline_ria.beans.Item;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class AuctionsUtilities {
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
        Instant currentInstant = Instant.now();
        Instant deadlineInstant = deadline.toInstant();
        Duration duration = Duration.between(currentInstant, deadlineInstant);

        if (duration.isNegative()) {
            return "Asta scaduta!";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        String time = "";

        if (days == 1) {
            time = "1 giorno";
        } else {
            time = days + " giorni";
        }
        if (hours == 1) {
            time += " 1 ora";
        } else {
            time += " " + hours + " ore";
        }
        if (minutes > 0) {
            if(minutes == 1)
                time += " 1 minuto";
            else
                time += " " + minutes + " minuti";
        }else if(seconds > 0){
            if(seconds == 1)
                time += " 1 secondo";
            else
                time += " " + seconds + " secondi";
        }
        return time;
    }

    public static String createItemsCodeName(List<Item> items) {
        String itemsCodeName = "";
        for (Item item : items) {
            itemsCodeName += "["+item.getCode() + "]" +"-" +item.getName() + " ";
        }
        return itemsCodeName;
    }


}
