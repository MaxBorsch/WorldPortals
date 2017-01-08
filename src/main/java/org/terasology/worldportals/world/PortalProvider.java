/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@Produces(PortalFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 4)))
public class PortalProvider implements FacetProviderPlugin {

    private Noise noise;

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);

        Border3D border = region.getBorderForFacet(PortalFacet.class);
        //Extend border by max radius + max length + max outer length and max lakedepth
        //border = border.extendBy(8,8,8);
        PortalFacet facet = new PortalFacet(region.getRegion(), border);
        Region3i worldRegion = facet.getWorldRegion();


        for (Vector3i pos : worldRegion) {
            float sHeight = surfaceHeightFacet.getWorld(pos.x(),pos.z());
            float noiseValue = noise.noise(pos.x(),pos.y(),pos.z());

            if (pos.y()==Math.round(sHeight) && noiseValue > 0.9993 && pos.x() < worldRegion.maxX() - 8 && pos.z() < worldRegion.maxY() - 8) {
                facet.setWorld(pos.x(), (int)sHeight, pos.z(), new Portal());
                break;
            }


        }

        region.setRegionFacet(PortalFacet.class, facet);
    }
}
