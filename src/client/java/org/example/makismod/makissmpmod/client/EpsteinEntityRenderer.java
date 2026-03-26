package org.example.makismod.makissmpmod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier; // Using Identifier as requested
import org.example.makismod.makissmpmod.entity.EpsteinEntity;

public class EpsteinEntityRenderer extends MobRenderer<EpsteinEntity, EpsteinEntityRenderState, EpsteinEntityModel> {

    // In 1.21.2, use Identifier.of instead of 'new'
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("makissmpmod", "textures/entity/epstein.png");

    public EpsteinEntityRenderer(EntityRendererProvider.Context context) {
        // Ensure you have EpsteinModelLayers.EPSTEIN defined in a separate class
        super(context, new EpsteinEntityModel(context.bakeLayer(EpsteinModelLayers.EPSTEIN)), 0.5f);
    }

    @Override
    public EpsteinEntityRenderState createRenderState() {
        return new EpsteinEntityRenderState();
    }

    @Override
    public void extractRenderState(EpsteinEntity entity, EpsteinEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        // Transfer data from the Entity to the RenderState
        state.yawDegrees = entity.getViewYRot(partialTick);
        state.pitchDegrees = entity.getViewXRot(partialTick);
    }

    @Override
    public Identifier getTextureLocation(EpsteinEntityRenderState state) {
        return TEXTURE;
    }
}