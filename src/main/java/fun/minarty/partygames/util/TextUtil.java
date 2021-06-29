package fun.minarty.partygames.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

    private static final String REMAINING_DURATION_FORMAT = "%02d:%02d";

    public String formatGameDuration(int remaining){
        return String.format(REMAINING_DURATION_FORMAT, remaining / 60, remaining % 60);
    }

}