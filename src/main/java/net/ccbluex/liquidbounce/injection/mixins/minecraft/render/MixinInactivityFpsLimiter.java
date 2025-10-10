/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.injection.mixins.minecraft.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.FpsLimitEvent;
import net.ccbluex.liquidbounce.utils.render.RefreshRateKt;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = InactivityFpsLimiter.class, priority = 100)
public abstract class MixinInactivityFpsLimiter {

    /**
     * Removes frame rate limit
     */
    @ModifyConstant(method = "update", constant = @Constant(intValue = 60), require = 0)
    private int getFramerateLimit(int original) {
        return RefreshRateKt.getRefreshRate();
    }

    @ModifyReturnValue(method = "update", at = @At("RETURN"))
    private int hookFpsLimit(int original) {
        return EventManager.INSTANCE.callEvent(new FpsLimitEvent(original)).getFps();
    }

}
