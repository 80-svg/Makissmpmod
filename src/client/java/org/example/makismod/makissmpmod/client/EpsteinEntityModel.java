package org.example.makismod.makissmpmod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jspecify.annotations.NonNull;

public class EpsteinEntityModel extends EntityModel<EpsteinEntityRenderState> {
    private final ModelPart head;

    public EpsteinEntityModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

//    public static LayerDefinition createBodyLayer() {
//    }
}