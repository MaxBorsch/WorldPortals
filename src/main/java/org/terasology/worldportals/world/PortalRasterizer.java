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

import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.worldportals.PortalSystem;

import java.util.Map.Entry;

public abstract class PortalRasterizer implements WorldRasterizerPlugin {

    @In
    PortalSystem portalSystem;

    private Block structureBlock;
    private Block portal;

    private String structureBlockName;
    private String portalBlockName;

    public PortalRasterizer () {
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
            Vector3i centerHousePosition = new Vector3i(entry.getKey());
            Vector3i extent = entry.getValue().getExtent();
            centerHousePosition.add(0, extent.y(), 0);
            Region3i walls = Region3i.createFromCenterExtents(centerHousePosition, new Vector3i(extent.x(), extent.y(), 0));
            Region3i inside = Region3i.createFromCenterExtents(centerHousePosition, new Vector3i(extent.x() - 1, extent.y() - 1, 0));

            for (Vector3i newBlockPosition : walls) {
                if (chunkRegion.getRegion().encompasses(newBlockPosition) && !inside.encompasses(newBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(newBlockPosition), structureBlock);

                } else if (inside.encompasses(newBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(newBlockPosition), portal);

                    PortalSystem.generatePortal(newBlockPosition, getDestination(newBlockPosition));

                }
            }
        }
    }

    public abstract Vector3f getDestination (Vector3i portalPosition);
}
