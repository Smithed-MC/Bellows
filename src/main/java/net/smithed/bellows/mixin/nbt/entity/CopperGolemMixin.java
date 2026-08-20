package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CopperGolem.class)
public abstract class CopperGolemMixin extends LivingEntityMixin {

    @Shadow private long nextWeatheringTick;

    @Shadow public WeatheringCopper.WeatherState getWeatherState() { return null; }
    @Shadow public void setWeatherState(final WeatheringCopper.WeatherState state) {}

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }

        switch (topLevelNbt) {
            case "next_weather_age" -> output.putLong("next_weather_age", this.nextWeatheringTick);
            case "weather_state" -> output.store("weather_state", WeatheringCopper.WeatherState.CODEC, this.getWeatherState());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }

        switch (topLevelNbt) {
            case "next_weather_age" -> this.nextWeatheringTick = input.getLongOr("next_weather_age", -1L);
            case "weather_state" -> this.setWeatherState(input.read("weather_state", WeatheringCopper.WeatherState.CODEC).orElse(WeatheringCopper.WeatherState.UNAFFECTED));
            default -> {
                return false;
            }
        }
        return true;
    }
}
