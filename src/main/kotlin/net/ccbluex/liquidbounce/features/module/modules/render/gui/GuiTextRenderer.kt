package net.ccbluex.liquidbounce.features.module.modules.render.gui

import net.ccbluex.liquidbounce.render.*
import net.ccbluex.liquidbounce.render.engine.font.FontRendererBuffers
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.client.gui.DrawContext

object GuiTextRenderer {
    
    fun drawSfBoldText(
        context: DrawContext,
        text: String,
        x: Float,
        y: Float,
        color: Color4b = Color4b.WHITE
    ) {
        // Kiểm tra xem font đã sẵn sàng chưa, nếu chưa thì thử load lại
        if (!SfBoldFontManager.isFontReady()) {
            // Có thể thử load lại một lần nữa nếu chưa sẵn sàng
            // Nhưng để tránh lặp vô hạn, mình chỉ kiểm tra
        }
        
        val sfBoldRenderer = SfBoldFontManager.getSfBoldFontRenderer()
        
        if (sfBoldRenderer != null) {
            renderEnvironmentForGUI { 
                val fontBuffers = FontRendererBuffers()
                
                sfBoldRenderer.draw(
                    sfBoldRenderer.process(text, color),
                    x,
                    y,
                    shadow = false,
                    z = 0.0f
                )
                
                sfBoldRenderer.commit(this, fontBuffers)
            }
        } else {
            // Fallback: dùng DrawContext.drawText với cảnh báo
            context.drawText(mc.textRenderer, text, x.toInt(), y.toInt(), color.toARGB(), false)
            // Có thể in log để debug nếu cần
            // println("SF Bold font not ready, using fallback font for: $text")
        }
    }
    
    fun drawSfBoldTextWithShadow(
        context: DrawContext,
        text: String,
        x: Float,
        y: Float,
        color: Color4b = Color4b.WHITE
    ) {
        val sfBoldRenderer = SfBoldFontManager.getSfBoldFontRenderer()
        
        if (sfBoldRenderer != null) {
            renderEnvironmentForGUI { 
                val fontBuffers = FontRendererBuffers()
                
                sfBoldRenderer.draw(
                    sfBoldRenderer.process(text, color),
                    x,
                    y,
                    shadow = true,
                    z = 0.0f
                )
                
                sfBoldRenderer.commit(this, fontBuffers)
            }
        } else {
            // Fallback với shadow - chuyển Float sang Int
            context.drawTextWithShadow(mc.textRenderer, text, x.toInt(), y.toInt(), color.toARGB())
        }
    }
    
    fun getSfBoldTextWidth(text: String): Float {
        val sfBoldRenderer = SfBoldFontManager.getSfBoldFontRenderer()
        return if (sfBoldRenderer != null) {
            val processedText = sfBoldRenderer.process(text, Color4b.WHITE)
            sfBoldRenderer.getStringWidth(processedText, false)
        } else {
            mc.textRenderer.getWidth(text).toFloat()
        }
    }
}