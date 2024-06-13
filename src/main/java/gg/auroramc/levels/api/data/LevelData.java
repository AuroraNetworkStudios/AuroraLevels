package gg.auroramc.levels.api.data;

import com.google.common.util.concurrent.AtomicDouble;
import gg.auroramc.aurora.api.user.UserDataHolder;
import gg.auroramc.aurora.api.util.NamespacedId;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LevelData extends UserDataHolder {
    private final AtomicInteger level = new AtomicInteger(0);
    private final AtomicDouble currentXP = new AtomicDouble(0);

    @Override
    public NamespacedId getId() {
        return NamespacedId.fromDefault("level");
    }

    @Override
    public void serializeInto(ConfigurationSection data) {
        data.set("level", level.get());
        data.set("currentXP", currentXP.get());
    }

    @Override
    public void initFrom(ConfigurationSection data) {
        if (data == null) return;
        level.set(data.getInt("level", 0));
        currentXP.set(data.getDouble("currentXP", 0));
    }

    public int getLevel() {
        return level.get();
    }

    public double getCurrentXP() {
        return currentXP.get();
    }

    public void setLevel(int level) {
        this.level.set(level);
        dirty.set(true);
    }

    public void setCurrentXP(double currentXP) {
        this.currentXP.set(currentXP);
        dirty.set(true);
    }
}
