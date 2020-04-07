# World Portals

*This module is aimed as a depency for other modules, and cannot be used directly by the player*

This module adds support for teleportation portals. Portals currently occupy a block and transport any entities that enter the occupied block to the portal's destination world position.

## Implementation

### PortalComponent
A portal component represents a one way portal and has a block coordinate location and a world position destination.

### PortalRasterizer
An abstract class that generates rectangular portal structures in the world, to be implemented in modules that need quick portals in the world.

### Sample Implementation
Underworld portal from the [Underworld module](https://github.com/Terasology/Underworld):

![Underworld Portal](https://github.com/MaxBorsch/Underworld/raw/master/preview/Portal.jpg?raw=true)

### Future Features

- Block-linked portals that remove when the block is removed.
- Cross world portals

# License
  
This module is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
