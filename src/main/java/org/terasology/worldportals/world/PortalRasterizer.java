/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.worldportals.world;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.worldportals.component.PortalComponent;

import java.util.Map.Entry;

public abstract class PortalRasterizer implements WorldRasterizerPlugin {

    @In
    EntityManager entityManager;

    private Block structureBlock;
    private Block portal;

    private String structureBlockName;
    private String portalBlockName;

    public PortalRasterizer (Vector3i destination) {
        this ("Core:Stone", "Core:Glass");
    }

    public PortalRasterizer (String structureBlockName, String portalBlockName) {
        this.structureBlockName = structureBlockName;
        this.portalBlockName = portalBlockName;
    }

    @Override
    public void initialize() {
        structureBlock = CoreRegistry.get(BlockManager.class).getBlock(structureBlockName);
        portal = CoreRegistry.get(BlockManager.class).getBlock(portalBlockName);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        PortalFacet portalFacet = chunkRegion.getFacet(PortalFacet.class);

        for (Entry<BaseVector3i, Portal> entry : portalFacet.getWorldEntries().entrySet()) {
            // there should be a house here
            // create a couple 3d regions to help iterate through the cube shape, inside and out
            Vector3i centerHousePosition = new Vector3i(entry.getKey());
            int extent = entry.getValue().getExtent();
            centerHousePosition.add(0, extent, 0);
            Region3i walls = Region3i.createFromCenterExtents(centerHousePosition, new Vector3i(extent, extent, 0));
            Region3i inside = Region3i.createFromCenterExtents(centerHousePosition, new Vector3i(extent - 1, extent - 1, 0));

            // loop through each of the positions in the cube, ignoring the is
            for (Vector3i newBlockPosition : walls) {
                if (chunkRegion.getRegion().encompasses(newBlockPosition) && !inside.encompasses(newBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcBlockPos(newBlockPosition), structureBlock);
                } else if (inside.encompasses(newBlockPosition)) {
                    Block portalBlock = chunk.setBlock(ChunkMath.calcBlockPos(newBlockPosition), portal);
                    EntityRef portalEntity = portalBlock.getEntity();

                    if (!portalEntity.exists()) {
                        EntityBuilder entityBuilder = entityManager.newBuilder();
                        entityBuilder.addComponent(new PortalComponent(getDestination(newBlockPosition)));
                        portalEntity = entityBuilder.build();

                        portalBlock.setEntity(portalEntity);

                    } else if (portalEntity.hasComponent(PortalComponent.class)) {
                        portalEntity.getComponent(PortalComponent.class).destination = getDestination(newBlockPosition);
                    } else {
                        portalEntity.addComponent(new PortalComponent(getDestination(newBlockPosition)));
                    }

                }
            }
        }
    }

    public abstract Vector3i getDestination (Vector3i portalPosition);
}
