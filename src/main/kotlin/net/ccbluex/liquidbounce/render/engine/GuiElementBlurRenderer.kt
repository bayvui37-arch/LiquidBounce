package net.ccbluex.liquidbounce.render.engine

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.ccbluex.liquidbounce.common.GlobalFramebuffer
import net.ccbluex.liquidbounce.render.shader.BlitShader
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.io.resourceToString
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.util.Window
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

object GuiElementBlurRenderer {
    
    // Shader cho hiệu ứng blur cục bộ
    private object ElementBlurShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/sobel.vert"),
        resourceToString("/resources/liquidbounce/shaders/blur/ui_blur.frag"),
    )
    
    private var isDrawingElementFramebuffer = false
    private var scissorEnabled = false
    
    private val elementFramebuffer by lazy {
        val fb = SimpleFramebuffer(
            mc.window.framebufferWidth,
            mc.window.framebufferHeight,
            true
        )
        fb.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        fb
    }
    
    private val tmpFramebuffer by lazy {
        val fb = SimpleFramebuffer(
            mc.window.framebufferWidth,
            mc.window.framebufferHeight,
            true
        )
        fb.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        fb
    }

    fun setupDimensions(width: Int, height: Int) {
        elementFramebuffer.resize(width, height)
        tmpFramebuffer.resize(width, height)
    }

    fun startGlassEffectDrawing(x: Int, y: Int, width: Int, height: Int) {
        // Không kiểm tra vsync để đảm bảo hiệu ứng luôn hoạt động
        // if (!mc.options.enableVsync.get()) return
        
        val window: Window = mc.window
        val scale = window.scaleFactor.toFloat()
        
        // Chuyển đổi tọa độ
        val glX = (x * scale).toInt()
        val glY = ((window.height - (y + height)) * scale).toInt()
        val glWidth = (width * scale).toInt()
        val glHeight = (height * scale).toInt()
        
        // Bật scissor test để giới hạn vùng áp dụng hiệu ứng
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(glX, glY, glWidth, glHeight)
        scissorEnabled = true
        
        // Vẽ nội dung vào framebuffer riêng
        GlobalFramebuffer.push(elementFramebuffer)
        elementFramebuffer.clear()
        elementFramebuffer.beginWrite(true)
        isDrawingElementFramebuffer = true
    }

    fun endGlassEffectDrawing(x: Int, y: Int, width: Int, height: Int) {
        if (!isDrawingElementFramebuffer || !scissorEnabled) return
        
        // Kết thúc vẽ vào framebuffer
        elementFramebuffer.endWrite()
        GlobalFramebuffer.pop()
        
        // Lưu trạng thái hiện tại
        val projectionMatrix = RenderSystem.getProjectionMatrix()
        val vertexSorting = RenderSystem.getProjectionType()

        // Copy framebuffer gốc vào tmp
        tmpFramebuffer.resize(mc.window.framebufferWidth, mc.window.framebufferHeight)
        tmpFramebuffer.beginWrite(false)
        mc.framebuffer.copyDepthFrom(mc.framebuffer)
        tmpFramebuffer.endWrite()

        // Áp dụng hiệu ứng blur lên framebuffer gốc
        mc.framebuffer.beginWrite(false)
        
        RenderSystem.disableBlend()
        ElementBlurShader.blit()
        RenderSystem.enableBlend()

        // Khôi phục trạng thái
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        scissorEnabled = false
        
        RenderSystem.setProjectionMatrix(projectionMatrix, vertexSorting)
        RenderSystem.defaultBlendFunc()
        
        isDrawingElementFramebuffer = false
    }
}