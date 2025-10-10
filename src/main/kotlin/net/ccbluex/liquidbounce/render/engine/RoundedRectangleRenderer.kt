package net.ccbluex.liquidbounce.render.engine

import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.minecraft.client.gui.DrawContext

object RoundedRectangleRenderer {
    
    /**
     * Draws a rounded rectangle using DrawContext by filling multiple small rectangles
     * 
     * @param context The DrawContext
     * @param x1, y1, x2, y2 The rectangle coordinates
     * @param radius The corner radius
     * @param color The color of the rectangle
     */
    fun drawRoundedRectangle(
        context: DrawContext,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        radius: Int
    ) {
        val width = x2 - x1
        val height = y2 - y1
        val actualRadius = minOf(radius, width / 2, height / 2)
        
        // Draw the center rectangle (the main body without corners)
        context.fill(
            x1 + actualRadius,
            y1,
            x2 - actualRadius,
            y2,
            -1 // default color, will be overridden by context's current settings if needed
        )
        
        // Draw the left and right center rectangles
        context.fill(
            x1,
            y1 + actualRadius,
            x1 + actualRadius,
            y2 - actualRadius,
            -1
        )
        context.fill(
            x2 - actualRadius,
            y1 + actualRadius,
            x2,
            y2 - actualRadius,
            -1
        )
        
        // Draw corners using multiple small rectangles to simulate rounded corners
        drawSimulatedRoundedCorner(context, x1, y1, actualRadius)  // Top-left
        drawSimulatedRoundedCorner(context, x2 - actualRadius, y1, actualRadius)  // Top-right
        drawSimulatedRoundedCorner(context, x1, y2 - actualRadius, actualRadius)  // Bottom-left
        drawSimulatedRoundedCorner(context, x2 - actualRadius, y2 - actualRadius, actualRadius)  // Bottom-right
    }
    
    /**
     * Draws a simulated rounded corner using multiple small rectangles
     */
    private fun drawSimulatedRoundedCorner(
        context: DrawContext,
        cornerX: Int,
        cornerY: Int,
        radius: Int
    ) {
        // Use multiple small rectangles to simulate the rounded corner
        val steps = 4 // Number of steps to approximate the curve (smaller = smoother but more expensive)
        val stepSize = kotlin.math.max(1, radius / steps)
        
        for (i in 0 until steps) {
            val currentRadius = (i + 1) * stepSize
            
            // Calculate the x/y offset based on the circle equation (x^2 + y^2 = r^2)
            val remainingRadius = radius - currentRadius
            val offset = kotlin.math.sqrt((radius * radius - remainingRadius * remainingRadius).toFloat()).toInt()
            
            // Draw a small rectangle for this part of the corner
            context.fill(
                cornerX,
                cornerY + radius - currentRadius,
                cornerX + offset,
                cornerY + radius - currentRadius + stepSize,
                -1
            )
        }
    }
    
    /**
     * Draw a rounded rectangle with a specific color
     */
    fun drawRoundedRectangle(
        context: DrawContext,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        radius: Int,
        color: Color4b
    ) {
        // Unfortunately, DrawContext.fill doesn't directly accept Color4b with alpha
        // We'll use the ARGB int format
        val argbColor = color.toARGB()
        
        val width = x2 - x1
        val height = y2 - y1
        val actualRadius = minOf(radius, width / 2, height / 2)
        
        // Draw the center rectangle (the main body without corners)
        context.fill(
            x1 + actualRadius,
            y1,
            x2 - actualRadius,
            y2,
            argbColor
        )
        
        // Draw the left and right center rectangles
        context.fill(
            x1,
            y1 + actualRadius,
            x1 + actualRadius,
            y2 - actualRadius,
            argbColor
        )
        context.fill(
            x2 - actualRadius,
            y1 + actualRadius,
            x2,
            y2 - actualRadius,
            argbColor
        )
        
        // For corners, we'll use a simple approximation with multiple rectangles
        // The exact algorithm needs to be simplified for DrawContext
        drawSimulatedRoundedCornerWithColor(context, x1, y1, actualRadius, argbColor)  // Top-left
        drawSimulatedRoundedCornerWithColor(context, x2 - actualRadius, y1, actualRadius, argbColor)  // Top-right
        drawSimulatedRoundedCornerWithColor(context, x1, y2 - actualRadius, actualRadius, argbColor)  // Bottom-left
        drawSimulatedRoundedCornerWithColor(context, x2 - actualRadius, y2 - actualRadius, actualRadius, argbColor)  // Bottom-right
    }
    
    private fun drawSimulatedRoundedCornerWithColor(
        context: DrawContext,
        cornerX: Int,
        cornerY: Int,
        radius: Int,
        color: Int
    ) {
        val steps = 4
        val stepSize = kotlin.math.max(1, radius / steps)
        
        for (i in 0 until steps) {
            val currentRadius = (i + 1) * stepSize
            
            val remainingRadius = radius - currentRadius
            val offset = kotlin.math.sqrt((radius * radius - remainingRadius * remainingRadius).toFloat()).toInt()
            
            // Draw a small rectangle for this part of the corner
            context.fill(
                cornerX,
                cornerY + radius - currentRadius,
                cornerX + offset,
                cornerY + radius - currentRadius + stepSize,
                color
            )
        }
    }
}