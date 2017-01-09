package org.terasology.worldportals.world;

import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Region;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map.Entry;

@Renders(value = PortalFacet.class, order = 5000)
public class PortalFacetLayer extends AbstractFacetLayer {

    private Color fillColor = new Color(224, 128, 128, 128);
    private Color frameColor = new Color(224, 128, 128, 224);

    @Override
    public void render(BufferedImage img, Region region) {
        PortalFacet portalFacet = region.getFacet(PortalFacet.class);

        Graphics2D g = img.createGraphics();

        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);

        for (Entry<BaseVector3i, Portal> entry : portalFacet.getWorldEntries().entrySet()) {
            Vector3i extent = entry.getValue().getExtent();

            BaseVector3i center = entry.getKey();
            g.setColor(fillColor);
            g.fillRect(center.x() - extent.x(), center.z() - extent.y(), 2 * extent.x(), 2 * extent.y());
            g.setColor(frameColor);
            g.drawRect(center.x() - extent.x(), center.z() - extent.y(), 2 * extent.x(), 2 * extent.y());
        }

        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        PortalFacet portalFacet = region.getFacet(PortalFacet.class);

        for (Entry<BaseVector3i, Portal> entry : portalFacet.getWorldEntries().entrySet()) {
            Vector3i extent = entry.getValue().getExtent();

            BaseVector3i center = entry.getKey();
            Vector2i min = new Vector2i(center.x() - extent.x(), center.z() - extent.y());
            Vector2i max = new Vector2i(center.x() + extent.x(), center.z() + extent.y());
            if (Rect2i.createFromMinAndMax(min, max).contains(wx, wy)) {
                return "Portal";
            }
        }

        return null;
    }

}
