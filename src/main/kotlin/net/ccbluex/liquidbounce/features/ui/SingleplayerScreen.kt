package net.ccbluex.liquidbounce.features.ui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.text.Text

class SingleplayerScreen : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .verticalAlignment(VerticalAlignment.TOP)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .padding(Insets.of(10))

        rootComponent.child(
            Components.label(Text.literal("Singleplayer"))
                .shadow(true)
                .margins(Insets.bottom(10))
        )

        val worldList = Containers.verticalFlow(Sizing.content(), Sizing.content())
        rootComponent.child(worldList.margins(Insets.top(10)))

        try {
            val summaries = mc.levelStorage.levelList.levels()
            summaries.forEach { world ->
                worldList.child(Components.label(world.displayName))
            }
        } catch (e: Exception) {
            worldList.child(Components.label(Text.literal("Could not load worlds: ${e.message}")))
        }


        rootComponent.child(
            Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.button(Text.literal("Back")) {
                    mc.execute {
                        mc.setScreen(TitleScreen())
                    }
                })
                .margins(Insets.top(10))
        )
    }
}
