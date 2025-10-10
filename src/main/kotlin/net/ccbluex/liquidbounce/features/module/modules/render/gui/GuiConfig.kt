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
package net.ccbluex.liquidbounce.features.module.modules.render.gui

import net.ccbluex.liquidbounce.utils.client.mc
import java.io.File
import java.util.Properties

/**
 * Configuration system for GUI appearance and behavior
 */
object GuiConfig {
    private val configFile = File(mc.runDirectory, "config/liquidbounce_gui.properties")
    private val properties = Properties()
    
    // Default values
    private const val DEFAULT_GRID_SIZE = 20
    private const val DEFAULT_PANEL_WIDTH = 18 // Doubled
    private const val DEFAULT_PANEL_MAX_HEIGHT = 14  // Made 2x smaller
    private const val DEFAULT_MODULE_HEIGHT = 6  // Made 3x smaller but still readable
    private const val DEFAULT_HEADER_HEIGHT = 6  // Made 3x smaller but still readable
    private const val DEFAULT_SEARCH_KEY = 342 // F3
    
    // Color defaults - much lighter and more visible
    // Further reduced to 44% opacity for lighter appearance
    private const val DEFAULT_BACKGROUND_COLOR = 0x70000000.toInt()
    // Much lighter header for better visibility
    private const val DEFAULT_HEADER_COLOR = 0xFF555555.toInt()
    // Brighter accent for better visibility
    private const val DEFAULT_ACCENT_COLOR = 0xFF22CCFF.toInt()
    // Much lighter border for better definition
    private const val DEFAULT_BORDER_COLOR = 0xFF888888.toInt()
    // Bright white text for maximum visibility
    private const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF.toInt()
    // Brighter enabled module color
    private const val DEFAULT_ENABLED_MODULE_COLOR = 0xFF0088CC.toInt()
    // Lighter hover color for better contrast
    private const val DEFAULT_HOVER_COLOR = 0xFF666666.toInt()
    
    init {
        loadConfig()
    }
    
    private fun loadConfig() {
        try {
            if (configFile.exists()) {
                configFile.inputStream().use { properties.load(it) }
            } else {
                // Set defaults if config doesn't exist
                setDefaults()
                saveConfig()
            }
        } catch (e: Exception) {
            println("Error loading GUI config: ${e.message}")
            setDefaults()
        }
    }
    
    private fun setDefaults() {
        properties.setProperty("grid.size", DEFAULT_GRID_SIZE.toString())
        properties.setProperty("panel.width", DEFAULT_PANEL_WIDTH.toString())
        properties.setProperty("panel.maxHeight", DEFAULT_PANEL_MAX_HEIGHT.toString())
        properties.setProperty("module.height", DEFAULT_MODULE_HEIGHT.toString())
        properties.setProperty("header.height", DEFAULT_HEADER_HEIGHT.toString())
        properties.setProperty("search.key", DEFAULT_SEARCH_KEY.toString())
        
        // Colors
        properties.setProperty("color.background", DEFAULT_BACKGROUND_COLOR.toString())
        properties.setProperty("color.header", DEFAULT_HEADER_COLOR.toString())
        properties.setProperty("color.accent", DEFAULT_ACCENT_COLOR.toString())
        properties.setProperty("color.border", DEFAULT_BORDER_COLOR.toString())
        properties.setProperty("color.text", DEFAULT_TEXT_COLOR.toString())
        properties.setProperty("color.enabledModule", DEFAULT_ENABLED_MODULE_COLOR.toString())
        properties.setProperty("color.hover", DEFAULT_HOVER_COLOR.toString())
    }
    
    fun saveConfig() {
        try {
            configFile.parentFile?.mkdirs()
            configFile.outputStream().use { 
                properties.store(it, "LiquidBounce GUI Configuration") 
            }
        } catch (e: Exception) {
            println("Error saving GUI config: ${e.message}")
        }
    }
    
    // Grid settings
    val gridSize: Int
        get() = properties.getProperty("grid.size", DEFAULT_GRID_SIZE.toString()).toIntOrNull() ?: DEFAULT_GRID_SIZE
    
    // Panel settings
    val panelWidth: Int
        get() = properties.getProperty("panel.width", DEFAULT_PANEL_WIDTH.toString())
            .toIntOrNull() ?: DEFAULT_PANEL_WIDTH
    
    val panelMaxHeight: Int
        get() = properties.getProperty("panel.maxHeight", DEFAULT_PANEL_MAX_HEIGHT.toString())
            .toIntOrNull() ?: DEFAULT_PANEL_MAX_HEIGHT
    
    val moduleHeight: Int
        get() = properties.getProperty("module.height", DEFAULT_MODULE_HEIGHT.toString())
            .toIntOrNull() ?: DEFAULT_MODULE_HEIGHT
    
    val headerHeight: Int
        get() = properties.getProperty("header.height", DEFAULT_HEADER_HEIGHT.toString())
            .toIntOrNull() ?: DEFAULT_HEADER_HEIGHT
    
    // Input settings
    val searchKey: Int
        get() = properties.getProperty("search.key", DEFAULT_SEARCH_KEY.toString()).toIntOrNull() ?: DEFAULT_SEARCH_KEY
    
    // Color settings
    val backgroundColor: Int
        get() = properties.getProperty("color.background", DEFAULT_BACKGROUND_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_BACKGROUND_COLOR
    
    val headerColor: Int
        get() = properties.getProperty("color.header", DEFAULT_HEADER_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_HEADER_COLOR
    
    val accentColor: Int
        get() = properties.getProperty("color.accent", DEFAULT_ACCENT_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_ACCENT_COLOR
    
    val borderColor: Int
        get() = properties.getProperty("color.border", DEFAULT_BORDER_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_BORDER_COLOR
    
    val textColor: Int
        get() = properties.getProperty("color.text", DEFAULT_TEXT_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_TEXT_COLOR
    
    val enabledModuleColor: Int
        get() = properties.getProperty("color.enabledModule", DEFAULT_ENABLED_MODULE_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_ENABLED_MODULE_COLOR
    
    val hoverColor: Int
        get() = properties.getProperty("color.hover", DEFAULT_HOVER_COLOR.toString())
            .toIntOrNull() ?: DEFAULT_HOVER_COLOR
    
    // Utility functions
    fun getGridSnappedPosition(x: Int, y: Int): Pair<Int, Int> {
        val snappedX = (x / gridSize) * gridSize
        val snappedY = (y / gridSize) * gridSize
        return Pair(snappedX, snappedY)
    }
    
    fun resetToDefaults() {
        setDefaults()
        saveConfig()
    }
}
