package org.valkyrienskies.tournament.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.valkyrienskies.core.api.ships.getAttachment
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.toJOMLD
import org.valkyrienskies.tournament.TournamentItems
import org.valkyrienskies.tournament.TournamentProperties
import org.valkyrienskies.tournament.ship.ThrusterShipControl
import org.valkyrienskies.tournament.util.DirectionalShape
import org.valkyrienskies.tournament.util.RotShapes
import java.util.*

class ThrusterBlock(
    val mult: Double,
    val particle: ParticleOptions,
    val maxTier: Int
) : DirectionalBlock (
    Properties.of(Material.STONE)
        .sound(SoundType.STONE).strength(1.0f, 2.0f)
) {

    val SHAPE = RotShapes.box(3.0, 5.0, 4.0, 13.0, 11.0, 16.0)

    val Thruster_SHAPE = DirectionalShape.south(SHAPE)

    init {
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.POWER, 0).setValue(TournamentProperties.TIER, 1))
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Thruster_SHAPE[state.getValue(BlockStateProperties.FACING)]
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (level as? ServerLevel == null) return InteractionResult.PASS
        if (player.mainHandItem.item.asItem().equals(TournamentItems.UPGRADE_THRUSTER.get()) && hand == InteractionHand.MAIN_HAND) {

            if (state.getValue(TournamentProperties.TIER) < maxTier) {

                level.setBlock(
                    pos,
                    state.setValue(
                        TournamentProperties.TIER,
                        (state.getValue(TournamentProperties.TIER) + 1)
                    ),
                    1
                )

                with(ThrusterShipControl.getOrCreate(
                    level.getShipObjectManagingPos(pos)
                        ?: level.getShipManagingPos(pos)
                        ?: return InteractionResult.CONSUME
                )) {
                    this.forceStopThruster(pos)
                    this.addThruster(
                        pos,
                        state.getValue(TournamentProperties.TIER).toDouble() * mult,
                        state.getValue(FACING)
                            .normal
                            .toJOMLD()
                            .mul(state.getValue(BlockStateProperties.POWER).toDouble())
                    )
                }

                return InteractionResult.CONSUME
            }
        }
        return InteractionResult.PASS
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(BlockStateProperties.POWER)
        builder.add(TournamentProperties.TIER)
        super.createBlockStateDefinition(builder)
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        super.onPlace(state, level, pos, oldState, isMoving)

        if (level.isClientSide) return
        level as ServerLevel

        val signal = level.getBestNeighborSignal(pos)
        level.setBlock(pos, state.setValue(BlockStateProperties.POWER, signal), 2)

        with(ThrusterShipControl.getOrCreate(
            level.getShipObjectManagingPos(pos)
                ?: level.getShipManagingPos(pos)
                ?: return
        )) {
            this.forceStopThruster(pos)
            this.addThruster(
                pos,
                state.getValue(TournamentProperties.TIER).toDouble() * mult,
                state.getValue(FACING)
                    .normal
                    .toJOMLD()
                    .mul(state.getValue(BlockStateProperties.POWER).toDouble())
            )
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        super.onRemove(state, level, pos, newState, isMoving)

        if (level.isClientSide) return
        level as ServerLevel

        state.setValue(BlockStateProperties.POWER, 0)
        level.getShipManagingPos(pos)?.getAttachment<ThrusterShipControl>()?.removeThruster(pos, state.getValue(TournamentProperties.TIER).toDouble(),state.getValue(FACING).normal.toJOMLD().mul(state.getValue(BlockStateProperties.POWER).toDouble()))
        level.getShipManagingPos(pos)?.getAttachment<ThrusterShipControl>()?.forceStopThruster( pos )
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        isMoving: Boolean
    ) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving)

        if (level as? ServerLevel == null) return

        val signal = level.getBestNeighborSignal(pos)
        if (state.getValue(BlockStateProperties.POWER) == signal) return

        level.setBlock(pos, state.setValue(BlockStateProperties.POWER, signal), 2)

        with(ThrusterShipControl.getOrCreate(
            level.getShipObjectManagingPos(pos)
                ?: level.getShipManagingPos(pos)
                ?: return
        )) {
            this.forceStopThruster(pos)
            this.addThruster(
                pos,
                state.getValue(TournamentProperties.TIER).toDouble() * mult,
                state.getValue(FACING)
                    .normal
                    .toJOMLD()
                    .mul(state.getValue(BlockStateProperties.POWER).toDouble())
            )
        }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        return defaultBlockState()
            .setValue(FACING, ctx.nearestLookingDirection)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: Random) {
        super.animateTick(state, level, pos, random)

        if (state.getValue(BlockStateProperties.POWER) > 0) {
            val dir = state.getValue(FACING)

            val x = pos.x.toDouble() + (0.5 * (dir.stepX + 1))
            val y = pos.y.toDouble() + (0.5 * (dir.stepY + 1))
            val z = pos.z.toDouble() + (0.5 * (dir.stepZ + 1))
            val speedX = dir.stepX * -0.4
            val speedY = dir.stepY * -0.4
            val speedZ = dir.stepZ * -0.4

            level.addParticle(particle, x, y, z, speedX, speedY, speedZ)
        }
    }

}