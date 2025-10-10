package net.ccbluex.liquidbounce.render.engine

import com.mojang.blaze3d.systems.RenderSystem
import net.ccbluex.liquidbounce.render.*
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.ccbluex.liquidbounce.render.engine.type.Vec3
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.io.resourceToString
import net.minecraft.util.math.Vec3d

object GuiBlurredRectRenderer {
    
    fun drawBlurredRect(x: Int, y: Int, width: Int, height: Int) {
        // Để áp dụng hiệu ứng blur thực sự từ shader, mình sẽ sử dụng
        // hệ thống render của LiquidBounce với màu trong suốt
        // để mô phỏng hiệu ứng glassmorphism
        
        renderEnvironmentForGUI { 
            // Vẽ hình chữ nhật với màu trắng trong suốt để mô phỏng hiệu ứng glass
            this.withColor(Color4b(255, 255, 255, 51)) { // ~20% opacity
                this.drawQuad(
                    Vec3(x.toDouble(), y.toDouble(), 0.0),
                    Vec3((x + width).toDouble(), (y + height).toDouble(), 0.0)
                )
            }
            
            // Thêm hiệu ứng viền mỏng để tạo cảm giác glass
            this.withColor(Color4b(255, 255, 255, 77)) { // ~30% opacity for border
                // Top border
                this.drawQuad(
                    Vec3(x.toDouble(), y.toDouble(), 0.0),
                    Vec3((x + width).toDouble(), (y + 1).toDouble(), 0.0)
                )
                // Bottom border
                this.drawQuad(
                    Vec3(x.toDouble(), (y + height - 1).toDouble(), 0.0),
                    Vec3((x + width).toDouble(), (y + height).toDouble(), 0.0)
                )
                // Left border
                this.drawQuad(
                    Vec3(x.toDouble(), y.toDouble(), 0.0),
                    Vec3((x + 1).toDouble(), (y + height).toDouble(), 0.0)
                )
                // Right border
                this.drawQuad(
                    Vec3((x + width - 1).toDouble(), y.toDouble(), 0.0),
                    Vec3((x + width).toDouble(), (y + height).toDouble(), 0.0)
                )
            }
        }
    }
}