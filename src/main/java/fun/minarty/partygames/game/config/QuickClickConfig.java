package fun.minarty.partygames.game.config;

import fun.minarty.partygames.model.config.DefaultConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import fun.minarty.partygames.game.QuickClickGame.ButtonRoom;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuickClickConfig extends DefaultConfig {

    private List<ButtonRoom> rooms;

}