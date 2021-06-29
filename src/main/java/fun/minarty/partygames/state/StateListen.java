package fun.minarty.partygames.state;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used inside game states to declare that we should listen <br>
 * to the event specified in the first parameter of the method body
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StateListen { }