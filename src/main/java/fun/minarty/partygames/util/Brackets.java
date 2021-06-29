package fun.minarty.partygames.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

public final class Brackets {

    private Brackets(){ }

    public static Component brackets(Type type, Component content, Style style){
        return Component.text(type.start, style)
                .append(content)
                .append(Component.text(type.end));
    }

    public enum Type {
        PARENTHESES('(', ')'),
        SQUARE('[', ']');

        private final char start;
        private final char end;

        Type(char start, char end) {
            this.start = start;
            this.end = end;
        }

    }

}
