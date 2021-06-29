package fun.minarty.partygames.util;

import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.manager.GameManager;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class ReflectionUtil {

    public Field getDeclaredFieldRecursive(Class<?> clazz, String name) {
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (Exception ignored) { }

            clazz = clazz.getSuperclass();
        }
        return field;
    }

    public boolean setConfigField(GameType type, GameConfig config, String name, Class<?> valueClass, Object value){

        Class<? extends GameConfig> clazz = GameManager.GAME_TYPES.get(type).getConfigClass();
        Field field = getDeclaredFieldRecursive(clazz, name);
        if(field == null)
            return false;

        if(field.getType() != valueClass)
            return false;

        field.setAccessible(true);

        try {
            field.set(config, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }


}