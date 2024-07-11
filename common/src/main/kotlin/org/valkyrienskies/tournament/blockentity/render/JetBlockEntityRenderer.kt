package org.valkyrienskies.tournament.blockentity.render

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import me.alex_s168.meshlib.Material
import me.alex_s168.meshlib.format.Format
import me.alex_s168.meshlib.texture.TextureCoordinate
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.resources.ResourceLocation
import org.valkyrienskies.tournament.TournamentMod.MOD_ID
import org.valkyrienskies.tournament.blockentity.JetBlockEntity
import org.valkyrienskies.tournament.util.extension.pose


class JetBlockEntityRenderer: BlockEntityRenderer<JetBlockEntity>{

    override fun render(
        blockEntity: JetBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val pos = blockEntity.blockPos
        poseStack.pose {
            val mod = MOD_ID

            fun loadFile(name: String): String {
                val content: StringBuilder = StringBuilder()
                val reader = Minecraft.getInstance().resourceManager.getResource(
                    ResourceLocation(mod, name)
                ).get().openAsReader()
                val lines = reader.lines()
                reader.close()
                lines.forEach { line -> content.append(line).append("\n")}
                return content.toString()
            }

            val unloaded = Format.OBJ.loadFrom(loadFile("something.obj"))
            val loaded = unloaded.load(::loadFile)
            loaded.verify()



            val toRender = loaded.groups.values.map { group ->
                val material: Material? = group.material?.let(loaded.materials::get)
                val texture = material?.diffuseMapFile?.let {
                    Minecraft
                        .getInstance()
                        .textureManager
                        .getTexture(ResourceLocation(mod, it))
                }

                texture to group.mesh
            }

            val pose = this.last().pose()
            toRender.forEach { (texture, mesh) ->
                texture?.bind()
                val tesselator = Tesselator.getInstance()
                val builder = tesselator.builder
                builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)

                //val shaders = ResourceLocation(mod, "shaders/post/shader.json")

                mesh.forEach { (tri, uv) ->
                    val normal = tri.normal

                    val a: TextureCoordinate? = uv?.a
                    val b: TextureCoordinate? = uv?.b
                    val c: TextureCoordinate? = uv?.c
                    builder.vertex(pose, tri.a.x + pos.x, tri.a.y + pos.y, tri.a.z + pos.z)
                        .uv(a?.u ?: 0f, a?.v ?: 0f)
                        .color(0, 0, 0, 0)
                        .normal(normal.x, normal.z, normal.x)
                        .endVertex()

                    builder.vertex(pose, tri.b.x + pos.x, tri.b.y + pos.y, tri.b.z + pos.z)
                        .uv(b?.u ?: 0f, b?.v ?: 0f)
                        .color(0, 0, 0, 0)
                        .normal(normal.x, normal.z, normal.x)
                        .endVertex()

                    builder.vertex(pose, tri.c.x + pos.x, tri.c.y + pos.y, tri.c.z + pos.z)
                        .uv(c?.u ?: 0f, c?.v ?: 0f)
                        .color(0, 0, 0, 0)
                        .normal(normal.x, normal.z, normal.x)
                        .endVertex()
                }

                tesselator.builder
            }
        }
    }
}