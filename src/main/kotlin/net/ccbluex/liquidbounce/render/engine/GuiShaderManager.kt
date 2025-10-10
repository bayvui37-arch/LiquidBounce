package net.ccbluex.liquidbounce.render.engine

import com.mojang.blaze3d.systems.RenderSystem
import net.ccbluex.liquidbounce.render.shader.BlitShader
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.io.resourceToString

object GuiShaderManager {
    
    // Shader cho hiệu ứng rect có blur
    object BlurredRectShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/core/rect/blurred_rect.vsh"),
        resourceToString("/resources/liquidbounce/shaders/core/rect/blurred_rect.fsh"),
    )
    
    // Shader cho hiệu ứng rect cơ bản
    object RectShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/core/rect/rect.vsh"),
        resourceToString("/resources/liquidbounce/shaders/core/rect/rect.fsh"),
    )
    
    // Shader cho hiệu ứng rect có texture
    object TexturedRectShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/core/rect/textured_rect.vsh"),
        resourceToString("/resources/liquidbounce/shaders/core/rect/textured_rect.fsh"),
    )
    
    // Các post-processing shader
    object BloomShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/post/bloom/bloom.vsh"),
        resourceToString("/resources/liquidbounce/shaders/post/bloom/bloom.fsh"),
    )
    
    object KawaseDownscaleShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/post/kawase/kawase.vsh"),
        resourceToString("/resources/liquidbounce/shaders/post/kawase/downscale.fsh"),
    )
    
    object KawaseUpscaleShader : BlitShader(
        resourceToString("/resources/liquidbounce/shaders/post/kawase/kawase.vsh"),
        resourceToString("/resources/liquidbounce/shaders/post/kawase/upscale.fsh"),
    )
}