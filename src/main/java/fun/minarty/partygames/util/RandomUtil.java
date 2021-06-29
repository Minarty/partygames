package fun.minarty.partygames.util;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class RandomUtil {

    public boolean chance(double chance){
        return ThreadLocalRandom.current().nextInt(100) <= (chance * (chance < 1 ? 100 : 1));
    }

    public <E> E getWeightedRandom(Map<E, Double> weights, Random random) {
        E result = null;
        double bestValue = Double.MAX_VALUE;

        for (E element : weights.keySet()) {
            double value = -Math.log(random.nextDouble()) / weights.get(element);

            if (value < bestValue) {
                bestValue = value;
                result = element;
            }
        }

        return result;
    }

}