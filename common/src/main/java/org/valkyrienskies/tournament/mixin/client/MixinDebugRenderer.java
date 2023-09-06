package org.valkyrienskies.tournament.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.tournament.TournamentDebugHelper;
import org.valkyrienskies.tournament.util.debug.DebugLine;
import org.valkyrienskies.tournament.util.helper.Helper3d;

import java.awt.*;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void postRender(final PoseStack matricesIgnore, final MultiBufferSource.BufferSource vertexConsumersIgnore,
                            final double cameraX, final double cameraY, final double cameraZ, final CallbackInfo ci) {
        final MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            TournamentDebugHelper.Companion.list().forEach((k,v)->{
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

                Vector3d cam = new Vector3d(cameraX, cameraY, cameraZ);

                if (v instanceof DebugLine line) {
                    assert Minecraft.getInstance().level != null;
                    Vector3d A = Helper3d.INSTANCE.convertShipToWorldSpace(Minecraft.getInstance().level, line.getA()).sub(cam);
                    Vector3d B = Helper3d.INSTANCE.convertShipToWorldSpace(Minecraft.getInstance().level, line.getB()).sub(cam);
                    Vector3d normalD = A.sub(B).normalize();
                    Vector3f normal = new Vector3f(
                            (float) normalD.x,
                            (float) normalD.y,
                            (float) normalD.z
                    );

                    Color c = line.getColor();

                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    int a = c.getAlpha();

                    vertexConsumer.vertex(A.x, A.y, A.z).color(r, g, b, a).normal(normal.x, normal.y, normal.z).endVertex();
                    vertexConsumer.vertex(B.x, B.y, B.z).color(r, g, b, a).normal(normal.x, normal.y, normal.z).endVertex();
                }
            });
            bufferSource.endBatch();
        }
    }

}
