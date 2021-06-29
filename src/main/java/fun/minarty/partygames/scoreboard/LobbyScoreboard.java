package fun.minarty.partygames.scoreboard;

import cc.pollo.gladeus.scoreboard.Scoreboard;
import cc.pollo.gladeus.scoreboard.builder.ScoreboardBuilder;
import fun.minarty.api.user.User;
import fun.minarty.grand.misc.TextFormatter;

public class LobbyScoreboard extends Scoreboard {

    private final TextFormatter textFormatter;
    private final User user;

    public LobbyScoreboard(User user,
                           TextFormatter textFormatter) {

        super(ScoreboardConstants.TITLE);

        this.textFormatter = textFormatter;
        this.user = user;
    }

    @Override
    public ScoreboardBuilder build() {
        return ScoreboardBuilder.builder(ScoreboardConstants.FORMAT, ScoreboardConstants.TRANSLATABLE_PREFIX)
                .empty()
                .formattedTranslatable("tickets", textFormatter.formatTickets(user.getTickets()))
                .formattedTranslatable("coins", textFormatter.formatBalance(user.getBalance()))
                .empty();
    }

}