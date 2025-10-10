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
@file:Suppress("WildcardImport")
package net.ccbluex.liquidbounce.features.module.modules.combat.killaura

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.config.types.NamedChoice
import net.ccbluex.liquidbounce.event.Sequence
import net.ccbluex.liquidbounce.event.events.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.events.SprintEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleAutoWeapon
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals.CriticalsSelectionMode
import net.ccbluex.liquidbounce.features.module.modules.combat.elytratarget.ModuleElytraTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRotationsConfigurable.KillAuraRotationTiming.ON_TICK
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRotationsConfigurable.KillAuraRotationTiming.SNAP
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.ModuleKillAura.RaycastMode.*
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraAutoBlock
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraFailSwing
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraFailSwing.dealWithFakeSwing
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraFightBot
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraNotifyWhenFail
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraNotifyWhenFail.failedHits
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features.KillAuraNotifyWhenFail.renderFailedHits
import net.ccbluex.liquidbounce.features.module.modules.misc.debugrecorder.modes.GenericDebugRecorder
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleDebug
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleDebug.debugGeometry
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleDebug.debugParameter
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.ccbluex.liquidbounce.render.renderEnvironmentForWorld
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.aiming.data.RotationWithVector
import net.ccbluex.liquidbounce.utils.aiming.point.PointTracker
import net.ccbluex.liquidbounce.utils.aiming.preference.LeastDifferencePreference
import net.ccbluex.liquidbounce.utils.aiming.utils.facingEnemy
import net.ccbluex.liquidbounce.utils.aiming.utils.raytraceBox
import net.ccbluex.liquidbounce.utils.aiming.utils.raytraceEntity
import net.ccbluex.liquidbounce.utils.combat.CombatManager
import net.ccbluex.liquidbounce.utils.combat.attack
import net.ccbluex.liquidbounce.utils.combat.shouldBeAttacked
import net.ccbluex.liquidbounce.utils.entity.rotation
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.isInventoryOpen
import net.ccbluex.liquidbounce.utils.inventory.isInContainerScreen
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.kotlin.random
import net.ccbluex.liquidbounce.utils.render.WorldTargetRenderer
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import kotlin.math.pow

/**
 * KillAura module
 *
 * Automatically attacks enemies.
 */
@Suppress("MagicNumber")
object ModuleKillAura : ClientModule("KillAura", Category.COMBAT) {

    // Attack speed
    val clickScheduler = tree(KillAuraClicker)

    // Range
    internal val range by float("Range", 4.2f, 1f..8f)
    internal val wallRange by float("WallRange", 3f, 0f..8f).onChange { wallRange ->
        if (wallRange > range) {
            range
        } else {
            wallRange
        }
    }

    private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged { range ->
        currentScanExtraRange = range.random()
    }
    private var currentScanExtraRange: Float = scanExtraRange.random()

    // SamePlayer feature  
    val samePlayer by boolean("SamePlayer", false)
    private val samePlayerDuration by int("SamePlayerDuration", 5, 1..120, "s") // Kept for compatibility but not used

    // Target
    val targetTracker = tree(KillAuraTargetTracker)

    // Rotation
    private val rotations = tree(KillAuraRotationsConfigurable)
    private val pointTracker = tree(PointTracker(this))

    private val requires by multiEnumChoice<KillAuraRequirements>("Requires")

    private val requirementsMet
        get() = requires.all { it.asBoolean }

    // Bypass techniques
    internal val raycast by enumChoice("Raycast", TRACE_ALL)
    private val criticalsSelectionMode by enumChoice("Criticals", CriticalsSelectionMode.SMART)
    private val keepSprint by boolean("KeepSprint", true)

    // Inventory Handling
    internal val ignoreOpenInventory by boolean("IgnoreOpenInventory", true)
    internal val simulateInventoryClosing by boolean("SimulateInventoryClosing", true)

    init {
        tree(KillAuraAutoBlock)
    }

    // Target rendering
    private val targetRenderer = tree(WorldTargetRenderer(this))

    init {
        tree(KillAuraFailSwing)
        tree(KillAuraFightBot)
    }

    override fun onDisabled() {
        targetTracker.reset()
        // Reset SamePlayer functionality when module is disabled
        targetTracker.stickyTarget = null
        targetTracker.stickyTimer.reset()
        failedHits.clear()
        KillAuraAutoBlock.stopBlocking()
        KillAuraNotifyWhenFail.failedHitsIncrement = 0
    }

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        val matrixStack = event.matrixStack

        renderTarget(matrixStack, event.partialTicks)
        renderFailedHits(matrixStack)
    }

    private fun renderTarget(matrixStack: MatrixStack, partialTicks: Float) {
        val target = targetTracker.target
            ?.takeIf { targetRenderer.enabled }
            ?.takeIf { !ModuleElytraTarget.isSameTargetRendering(it) }
            ?: return

        renderEnvironmentForWorld(matrixStack) {
            targetRenderer.render(this, target, partialTicks)
        }
    }

    @Suppress("unused")
    private val rotationUpdateHandler = handler<RotationUpdateEvent> {
        // Make sure killaura-logic is not running while inventory is open
        val isInInventoryScreen = isInventoryOpen || mc.currentScreen is GenericContainerScreen
        val shouldResetTarget = player.isSpectator || player.isDead || !requirementsMet

        if (isInInventoryScreen && !ignoreOpenInventory || shouldResetTarget) {
            // Reset current target
            targetTracker.reset()
            return@handler
        }

        // Update current target tracker to make sure you attack the best enemy
        updateTarget()

        // Update Auto Weapon
        ModuleAutoWeapon.onTarget(targetTracker.target)
    }

    @Suppress("unused")
    private val gameHandler = tickHandler {
        if (player.isDead || player.isSpectator) {
            return@tickHandler
        }

        // Check if there is target to attack
        val target = targetTracker.target

        if (CombatManager.shouldPauseCombat) {
            KillAuraAutoBlock.stopBlocking()
            return@tickHandler
        }

        if (target == null) {
            val hasUnblocked = KillAuraAutoBlock.stopBlocking()

            // Deal with fake swing when there is no target
            if (KillAuraFailSwing.enabled && requirementsMet) {
                if (hasUnblocked) {
                    waitTicks(KillAuraAutoBlock.currentTickOff)
                }
                dealWithFakeSwing(this, null)
            }
            return@tickHandler
        }

        // Check if the module should (not) continue after the blocking state is updated
        if (!requirementsMet) {
            return@tickHandler
        }

        val rotation = (if (rotations.rotationTiming == ON_TICK) {
            findRotation(target, range.toDouble())?.rotation
        } else {
            null
        } ?: RotationManager.currentRotation ?: player.rotation).normalize()

        val crosshairTarget = if (samePlayer && targetTracker.stickyTarget != null) {
            // If SamePlayer is on and there's a sticky target, always use it.
            targetTracker.stickyTarget
        } else {
            // Enhanced raycast: try to find target through other entities first
            val raycastTarget = when {
                raycast != TRACE_NONE -> {
                    // First try normal raycast
                    val normalResult = raytraceEntity(range.toDouble(), rotation, filter = {
                        when (raycast) {
                            TRACE_ONLYENEMY -> it.shouldBeAttacked()
                            TRACE_ALL -> true
                            else -> false
                        }
                    })?.entity
                    
                    // If no result and we have a preferred target, try cutthrough raycast
                    if (normalResult == null && target != null && target is LivingEntity) {
                        raytraceEntityThroughEntities(range.toDouble(), rotation, target) ?: target
                    } else {
                        normalResult ?: target
                    }
                }
                else -> target
            }

            if (raycastTarget is LivingEntity && raycastTarget.shouldBeAttacked() && raycastTarget != target) {
                raycastTarget
            } else {
                target
            }
        }

        // Smart blocking: don't attack wrong targets when SamePlayer is active
        if (samePlayer && targetTracker.stickyTarget != null && 
            crosshairTarget is LivingEntity && crosshairTarget != targetTracker.stickyTarget) {
            // Block attack on non-sticky targets
            return@tickHandler
        }

        // Update target tracker if needed
        if (crosshairTarget != targetTracker.target && crosshairTarget is LivingEntity) {
            targetTracker.target = crosshairTarget
        }

        attackTarget(this, crosshairTarget ?: return@tickHandler, rotation)
    }

    val shouldBlockSprinting
        get() = !ModuleElytraTarget.running
            && criticalsSelectionMode.shouldStopSprinting(clickScheduler, targetTracker.target)

    @Suppress("unused")
    private val sprintHandler = handler<SprintEvent> { event ->
        if (shouldBlockSprinting && (event.source == SprintEvent.Source.MOVEMENT_TICK ||
                event.source == SprintEvent.Source.INPUT)) {
            event.sprint = false
        }
    }

    @Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod")
    private suspend fun attackTarget(sequence: Sequence, target: Entity, rotation: Rotation) {
        // Make it seem like we are blocking
        KillAuraAutoBlock.makeSeemBlock()

        // Are we actually facing the [chosenEntity]
        var isFacingEnemy = facingEnemy(toEntity = target, rotation = rotation,
            range = range.toDouble(),
            wallsRange = wallRange.toDouble()) || ModuleElytraTarget.canIgnoreKillAuraRotations

        ModuleDebug.debugParameter(ModuleKillAura, "Is Facing Enemy", isFacingEnemy)
        ModuleDebug.debugParameter(ModuleKillAura, "Rotation", rotation)
        ModuleDebug.debugParameter(ModuleKillAura, "Target", target.nameForScoreboard)

        // In SamePlayer mode with sticky target, try to attack it even through other entities
        // This is our special "entity-through" functionality for SamePlayer
        if (samePlayer && targetTracker.stickyTarget == target && !isFacingEnemy) {
            // Try to raytrace through entities to reach the sticky target
            val rotationToSticky = (if (rotations.rotationTiming == KillAuraRotationsConfigurable.KillAuraRotationTiming.ON_TICK) {
                if (target is LivingEntity) {
                    findRotation(target, range.toDouble())?.rotation
                } else {
                    null
                }
            } else {
                null
            } ?: RotationManager.currentRotation ?: player.rotation).normalize()
            
            // Try to raytrace directly to the sticky target, ignoring other entities in between
            val entityThroughWall = raytraceEntityThroughEntities(range.toDouble(), rotationToSticky, target)
            if (entityThroughWall != null) {
                isFacingEnemy = true
            }
        }

        // Use the original aimThroughWalls functionality if not facing enemy and not using same player logic
        // or use it in addition to same player if needed for wall penetration
        if (!isFacingEnemy && rotations.aimThroughWalls) {
            // This is the original through-walls functionality (for blocks/obstacles)
            isFacingEnemy = facingEnemy(toEntity = target, rotation = rotation,
                range = range.toDouble(),
                wallsRange = range.toDouble())
        }

        // Check if our target is in range, otherwise deal with auto block
        if (!isFacingEnemy) {
            if (KillAuraAutoBlock.enabled && KillAuraAutoBlock.onScanRange &&
                player.squaredBoxedDistanceTo(target) <= (range + currentScanExtraRange).pow(2)) {
                KillAuraAutoBlock.startBlocking()
                return
            }

            // Make sure we are not blocking
            val hasUnblocked = KillAuraAutoBlock.stopBlocking()

            // Deal with fake swing
            if (KillAuraFailSwing.enabled) {
                if (hasUnblocked) {
                    sequence.waitTicks(KillAuraAutoBlock.currentTickOff)
                }

                dealWithFakeSwing(sequence, target)
            }
            return
        }

        ModuleDebug.debugParameter(ModuleKillAura, "Valid Rotation", rotation)

        // Attack enemy, according to the attack scheduler
        if (clickScheduler.isClickTick && validateAttack(target)) {
            clickScheduler.attack(sequence, rotation) {
                // On each click, we check if we are still ready to attack
                if (!validateAttack(target)) {
                    return@attack false
                }

                // Attack enemy
                target.attack(true, keepSprint && !shouldBlockSprinting)
                
                // Update sticky target when SamePlayer is enabled
                if (samePlayer && target is LivingEntity) {
                    targetTracker.stickyTarget = target
                    targetTracker.stickyTimer.reset()
                }
                
                currentScanExtraRange = scanExtraRange.random()
                KillAuraNotifyWhenFail.failedHitsIncrement = 0

                GenericDebugRecorder.recordDebugInfo(ModuleKillAura, "attackEntity", JsonObject().apply {
                    add("player", GenericDebugRecorder.debugObject(player))
                    add("targetPos", GenericDebugRecorder.debugObject(target))
                })

                true
            }
        } else if (KillAuraAutoBlock.currentTickOff > 0 && clickScheduler.willClickAt(KillAuraAutoBlock.currentTickOff)
            && KillAuraAutoBlock.shouldUnblockToHit) {
            KillAuraAutoBlock.stopBlocking(pauses = true)
        } else {
            KillAuraAutoBlock.startBlocking()
        }
    }

    private fun updateTarget() {
        // Calculate maximum range based on enemy distance
        val maximumRange = if (targetTracker.closestSquaredEnemyDistance > range.pow(2)) {
            range + currentScanExtraRange
        } else {
            range
        }

        debugParameter("Maximum Range") { maximumRange }
        debugParameter("Range") { range }
        val squaredMaxRange = maximumRange.pow(2)
        val squaredNormalRange = range.pow(2)

        // SamePlayer logic: if enabled and sticky target exists, prioritize it
        if (samePlayer) {
            val sticky = targetTracker.stickyTarget
            if (sticky != null) {
                // In your desired context: keep target until it dies or module is disabled
                // Only clear if target is no longer alive
                if (!sticky.isAlive) {
                    // Sticky target is no longer valid, clear it
                    targetTracker.stickyTarget = null
                    targetTracker.stickyTimer.reset() // Reset timer when clearing
                } else if (player.squaredBoxedDistanceTo(sticky) <= squaredMaxRange && sticky.shouldBeAttacked()) {
                    // Keep the sticky target if it's valid
                    targetTracker.target = sticky
                    
                    // Also process (aim at) the sticky target
                    processTarget(sticky, maximumRange)
                    return
                } else {
                    // Sticky target is out of range or not attackable,
                    // but SamePlayer is enabled so we still prioritize it if possible
                    targetTracker.target = sticky
                    
                    // Also process (aim at) the sticky target
                    processTarget(sticky, maximumRange)
                    return
                }
            }
        }

        // Only find a new target if SamePlayer is not enabled or no sticky target exists
        val target = targetTracker.targets()
            .filter { entity -> entity.squaredBoxedDistanceTo(player) <= squaredMaxRange }
            .sortedBy { entity -> if (entity.squaredBoxedDistanceTo(player) <= squaredNormalRange) 0 else 1 }
            .firstOrNull { entity -> processTarget(entity, maximumRange) }

        if (target != null) {
            targetTracker.target = target
            // If SamePlayer is enabled, set this new target as the sticky target
            if (samePlayer) {
                targetTracker.stickyTarget = target as? LivingEntity
                targetTracker.stickyTimer.reset() // Reset timer when new sticky target is set
            }
        } else if (KillAuraFightBot.enabled) {
            KillAuraFightBot.updateTarget()

            RotationManager.setRotationTarget(
                rotations.toRotationTarget(
                    KillAuraFightBot.getMovementRotation(),
                    considerInventory = !ignoreOpenInventory
                ),
                priority = Priority.IMPORTANT_FOR_USAGE_2,
                provider = ModuleKillAura
            )
        }

        // Clear sticky target if SamePlayer is disabled
        if (!samePlayer) {
            targetTracker.stickyTarget = null
        }
    }

    @Suppress("ReturnCount")
    private fun processTarget(
        entity: LivingEntity,
        range: Float
    ): Boolean {
        val (rotation, _) = findRotation(entity, range.toDouble()) ?: return false
        val ticks = rotations.calculateTicks(rotation)
        debugParameter("Rotation Ticks") { ticks }

        when (rotations.rotationTiming) {

            // If our click scheduler is not going to click the moment we reach the target,
            // we should not start aiming towards the target just yet.
            SNAP -> if (!clickScheduler.willClickAt(ticks.coerceAtLeast(1))) {
                return true
            }

            // [ON_TICK] will always instantly aim onto the target on attack, however, if
            // our rotation is unable to be ready in time, we can at least start aiming towards
            // the target.
            ON_TICK -> if (ticks <= 1) {
                return true
            }

            else -> {
                // Continue with regular aiming
            }
        }

        RotationManager.setRotationTarget(
            rotations.toRotationTarget(
                rotation,
                entity,
                considerInventory = !ignoreOpenInventory
            ),
            priority = Priority.IMPORTANT_FOR_USAGE_2,
            provider = this@ModuleKillAura
        )

        return true
    }

    /**
     * Get the best spot to attack the entity
     *
     * @param entity The entity to attack
     * @param range The range to attack the entity (NOT SQUARED)
     * @param ticks The ticks until we attack
     *
     *  @return The best spot to attack the entity
     */
    private fun findRotation(entity: LivingEntity, range: Double): RotationWithVector? {
        val eyes = player.eyePos
        val point = pointTracker.findPoint(eyes, entity)

        val pointPos = point.pos

        debugGeometry("Box") { ModuleDebug.DebuggedBox(point.box, Color4b.ORANGE.with(a = 90)) }
        debugGeometry("Point") { ModuleDebug.DebuggedPoint(pointPos, Color4b.WHITE, size = 0.1) }

        val rotationPreference = LeastDifferencePreference.leastDifferenceToLastPoint(eyes, pointPos)

        // raytrace to the point
        val rotation = raytraceBox(
            eyes = eyes,
            box = point.box,
            // Since [range] is squared, we need to square root
            range = range,
            wallsRange = wallRange.toDouble(),
            rotationPreference = rotationPreference
        )

        return if (rotation == null && rotations.aimThroughWalls) {
            val rotationThroughWalls = raytraceBox(
                eyes = eyes,
                box = point.box,
                // Since [range] is squared, we need to square root
                range = range,
                wallsRange = range,
                rotationPreference = rotationPreference
            )

            rotationThroughWalls
        } else {
            rotation
        }
    }

    /**
     * Check if we can attack the target at the current moment
     */
    internal fun validateAttack(target: Entity? = null): Boolean {
        val criticalHit = target == null || player.isGliding || criticalsSelectionMode.isCriticalHit(target)
        val isInInventoryScreen = isInventoryOpen || isInContainerScreen

        return criticalHit && !(isInInventoryScreen && !ignoreOpenInventory && !simulateInventoryClosing)
    }

    /**
     * Enhanced raycast that can find specific target through other entities
     * Automatically applied for better targeting
     */
     private fun raytraceEntityThroughEntities(range: Double, rotation: Rotation, targetEntity: Entity): Entity? {
        val world = mc.world ?: return null
        val player = mc.player ?: return null
        
        // Check if target entity is alive and within range first
        if (!targetEntity.isAlive || player.distanceTo(targetEntity) > range) {
            return null
        }
        
        val raycastVector = rotation.directionVector
        val eyes = player.eyePos
        val endPos = eyes.add(raycastVector.multiply(range))
        
        // Instead of normal raycast which stops at first entity, 
        // we'll check if our target entity's bounding box is reachable
        val targetBox = targetEntity.boundingBox
        val rayResult = targetBox.raycast(eyes, endPos)
        
        // If the ray hits the target entity's box, return the target,
        // regardless of other entities that might be in the way
        if (rayResult.isPresent) {
            return targetEntity
        }
        
        return null
    }

    enum class RaycastMode(override val choiceName: String) : NamedChoice {
        TRACE_NONE("None"),
        TRACE_ONLYENEMY("Enemy"),
        TRACE_ALL("All")
    }

}
