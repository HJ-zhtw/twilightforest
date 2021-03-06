package twilightforest.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.ModelTFIceCrystal;
import twilightforest.entity.boss.EntityTFIceCrystal;

public class RenderTFIceCrystal extends RenderLiving<EntityTFIceCrystal> {

	private static final ResourceLocation textureLoc = TwilightForestMod.getModelTexture("icecrystal.png");

	public RenderTFIceCrystal(RenderManager manager) {
		super(manager, new ModelTFIceCrystal(), 0.25F);
	}

	@Override
	protected void preRenderCallback(EntityTFIceCrystal entity, float partialTicks) {
		float bounce = entity.ticksExisted + partialTicks;
		GlStateManager.translate(0F, MathHelper.sin((bounce) * 0.2F) * 0.15F, 0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTFIceCrystal entity) {
		return textureLoc;
	}

}
