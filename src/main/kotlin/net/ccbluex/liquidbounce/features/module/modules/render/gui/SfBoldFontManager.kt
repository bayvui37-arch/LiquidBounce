package net.ccbluex.liquidbounce.features.module.modules.render.gui

import net.ccbluex.liquidbounce.render.FontManager
import kotlinx.coroutines.*
import java.io.File

object SfBoldFontManager {
    
    const val SF_BOLD_FONT_NAME = "SF Bold"
    private var isFontLoaded = false
    private var loadJob: Job? = null
    
    // Synchronous loading function to ensure font is loaded before use
    fun loadSfBoldFont() {
        if (isFontLoaded) return
        
        val fontFile = File("/workspaces/LiquidBounce/sf_bold.ttf")
        
        if (fontFile.exists()) {
            try {
                // Load the font synchronously to ensure it's available when needed
                runBlocking {
                    FontManager.queueFontFromFile(fontFile)
                }
                isFontLoaded = true
                println("Successfully loaded SF Bold font")
            } catch (e: Exception) {
                println("Failed to load SF Bold font: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("SF Bold font file not found at ${fontFile.absolutePath}")
        }
    }
    
    fun loadSfBoldFontAsync() {
        if (isFontLoaded || loadJob?.isActive == true) return
        
        val fontFile = File("/workspaces/LiquidBounce/sf_bold.ttf")
        
        if (fontFile.exists()) {
            loadJob = GlobalScope.launch(Dispatchers.IO) {
                try {
                    FontManager.queueFontFromFile(fontFile)
                    isFontLoaded = true
                    println("Successfully loaded SF Bold font")
                } catch (e: Exception) {
                    println("Failed to load SF Bold font: ${e.message}")
                    e.printStackTrace()
                }
            }
        } else {
            println("SF Bold font file not found at ${fontFile.absolutePath}")
        }
    }
    
    fun isFontReady() = isFontLoaded
    
    fun getSfBoldFontRenderer() = if (isFontLoaded) {
        FontManager.fontFace(SF_BOLD_FONT_NAME)?.renderer
    } else null
}