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
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.config.types.nesting.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.gui.ClickGuiScreen
import net.ccbluex.liquidbounce.utils.client.asText
import net.ccbluex.liquidbounce.utils.client.inGame
import net.minecraft.client.gui.screen.Screen
import org.lwjgl.glfw.GLFW

/**
 * ClickGUI module
 *
 * Shows you an easy-to-use menu to toggle and configure modules.
 */

object ModuleClickGui :
    ClientModule("ClickGUI", Category.RENDER, bind = GLFW.GLFW_KEY_RIGHT_SHIFT, disableActivation = true) {

    override val running = true

    @Suppress("UnusedPrivateProperty")
    private val scale by float("Scale", 1f, 0.5f..2f).onChanged {
        EventManager.callEvent(ClickGuiScaleChangeEvent(it))
        EventManager.callEvent(ClickGuiValueChangeEvent(this))
    }

    @Suppress("UnusedPrivateProperty")
    private val cache by boolean("Cache", true).onChanged { cache ->
        // Note: Cache setting no longer needed with native GUI
    }

    private val trackMousePosition by boolean("TrackMousePosition", false)

    @Suppress("UnusedPrivateProperty")
    private val searchBarAutoFocus by boolean("SearchBarAutoFocus", true).onChanged {
        EventManager.callEvent(ClickGuiValueChangeEvent(this))
    }

    val isInSearchBar: Boolean
        get() = mc.currentScreen is ClickGuiScreen
        // Note: Removed isTyping reference - native GUI handles this internally

    object Snapping : ToggleableConfigurable(this, "Snapping", true) {

        @Suppress("UnusedPrivateProperty")
        private val gridSize by int("GridSize", 10, 1..100, "px").onChanged {
            EventManager.callEvent(ClickGuiValueChangeEvent(ModuleClickGui))
        }

        init {
            inner.find { it.name == "Enabled" }?.onChanged {
                EventManager.callEvent(ClickGuiValueChangeEvent(ModuleClickGui))
            }
        }
    }

    // Note: Browser variable removed - no longer needed with native GUI
    // private var clickGuiBrowser: Browser? = null

    init {
        tree(Snapping)
    }

    override fun onEnabled() {
        // Pretty sure we are not in a game, so we can't open the clickgui
        if (!inGame) {
            return
        }

        // Use native ClickGUI implementation instead of JCEF
        mc.setScreen(ClickGuiScreen())
        super.onEnabled()
    }

    // Note: JCEF browser methods no longer needed with native GUI
    /*
    private fun open() {
        if (clickGuiBrowser != null) {
            return
        }

        clickGuiBrowser = ThemeManager.openInputAwareImmediate(
            VirtualScreenType.CLICK_GUI,
            true,
            priority = 20,
            settings = IntegrationListener.browserSettings
        ) {
            mc.currentScreen is ClickScreen
        }
    }

    private fun close() {
        clickGuiBrowser?.let {
            it.close()
            clickGuiBrowser = null
        }
    }

    fun reload(restart: Boolean = false) {
        if (restart) {
            close()
            open()
            return
        }

        clickGuiBrowser?.reload()
    }
    */

    // Note: Event handlers related to JCEF browser no longer needed
    /*
    @Suppress("unused")
    private val gameRenderHandler = handler<GameRenderEvent>(priority = OBJECTION_AGAINST_EVERYTHING) {
        clickGuiBrowser?.visible = mc.currentScreen is ClickScreen
    }

    @Suppress("unused")
    private val browserReadyHandler = handler<BrowserReadyEvent>(priority = READ_FINAL_STATE) {
        tree(IntegrationListener.browserSettings)
        open()
    }

    @Suppress("unused")
    private val worldChangeHandler = sequenceHandler<WorldChangeEvent>(
        priority = OBJECTION_AGAINST_EVERYTHING
    ) { event ->
        if (event.world == null) {
            return@sequenceHandler
        }

        waitSeconds(WORLD_CHANGE_SECONDS_UNTIL_RELOAD)
        if (mc.currentScreen !is ClickScreen) {
            reload()
        }
    }

    @Suppress("unused")
    private val clientLanguageChangedHandler = handler<ClientLanguageChangedEvent> {
        if (mc.currentScreen !is ClickScreen) {
            reload()
        }
    }
    */

    // Note: Mouse tracking variables removed - no longer needed with native GUI
    // private var mouseX = Double.NaN
    // private var mouseY = Double.NaN

    /**
     * An empty screen that acts as a hint when to draw the clickgui
     */
    class ClickScreen : Screen("ClickGUI".asText()) {

        override fun close() {
            mc.mouse.lockCursor()
            super.close()
        }

        override fun shouldPause(): Boolean {
            // preventing game pause
            return false
        }
    }

}
