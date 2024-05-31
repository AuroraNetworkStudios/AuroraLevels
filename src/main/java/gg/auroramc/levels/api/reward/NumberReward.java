package gg.auroramc.levels.api.reward;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.expression.NumberExpression;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class NumberReward extends AbstractReward {
    protected Double amount = null;
    protected String formula = null;

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);

        if(args.contains("amount")) {
            amount = args.getDouble("amount");
        }
        formula =  args.getString("formula", null);

        if (amount == null && formula == null) {
            AuroraLevels.logger().warning("NumberReward has neither the amount or the formula key");
        }
    }

    public Double getValue(List<Placeholder<?>> formulaPlaceholders) {
        if(amount != null) {
            return amount;
        } else if(formula != null) {
            return NumberExpression.eval(formula, formulaPlaceholders);
        }
        return 0.0;
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> formulaPlaceholders) {
        var value = getValue(formulaPlaceholders);

        return Placeholder.execute(display,
                Placeholder.of("{value}", value),
                Placeholder.of("{value_int}", value.longValue()),
                Placeholder.of("{value_formatted}", AuroraAPI.formatNumber(value)));
    }
}
