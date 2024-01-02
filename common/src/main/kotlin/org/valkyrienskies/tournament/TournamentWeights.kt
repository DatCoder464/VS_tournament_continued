package org.valkyrienskies.tournament

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.joml.Math
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.mod.common.BlockStateInfo
import org.valkyrienskies.mod.common.BlockStateInfoProvider
import org.valkyrienskies.physics_api.Lod1BlockStateId
import org.valkyrienskies.physics_api.Lod1LiquidBlockStateId
import org.valkyrienskies.physics_api.Lod1SolidBlockStateId
import org.valkyrienskies.physics_api.voxel.Lod1LiquidBlockState
import org.valkyrienskies.physics_api.voxel.Lod1SolidBlockState

object TournamentWeights  {

    fun register() {
        Registry.register(BlockStateInfo.REGISTRY, ResourceLocation(TournamentMod.MOD_ID, "ballast"), Ballast)
    }
    object Ballast: BlockStateInfoProvider {
        override val blockStateData: List<Triple<Lod1SolidBlockStateId, Lod1LiquidBlockStateId, Lod1BlockStateId>>
            get() = emptyList()

        override val liquidBlockStates: List<Lod1LiquidBlockState>
            get() = emptyList()

        override val priority: Int
            get() = 200

        override val solidBlockStates: List<Lod1SolidBlockState>
            get() = emptyList()

        override fun getBlockStateMass(blockState: BlockState): Double? =
            if (blockState.block == TournamentBlocks.BALLAST.get())
                Math.lerp(
                    TournamentConfig.SERVER.ballastNoWeight,
                    TournamentConfig.SERVER.ballastWeight,
                    (kotlin.runCatching { blockState.getValue(BlockStateProperties.POWER) }.getOrNull() ?: 0) / 15.0
                )
            else
                null

        override fun getBlockStateType(blockState: BlockState): BlockType? =
            null
    }
}
