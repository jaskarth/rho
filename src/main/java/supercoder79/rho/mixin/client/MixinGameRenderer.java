package supercoder79.rho.mixin.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import supercoder79.rho.RhoCompiler;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void renderRhoData(float f, long l, boolean bl, CallbackInfo ci, int i, int j, Window window, Matrix4f matrix4f, PoseStack poseStack, PoseStack poseStack2) {
        if (RhoCompiler.isCompilingCurrently) {
            Minecraft.getInstance().font.draw(poseStack2, "Rho: Compiling \"" + RhoCompiler.currentName + "\" at status \"" + RhoCompiler.currentStatus + "\"", 0, 0, 0xFFFFFF);
        }
    }
}
