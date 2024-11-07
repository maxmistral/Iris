/*
 * Iris is a World Generator for Minecraft Bukkit Servers
 * Copyright (c) 2022 Arcane Arts (Volmit Software)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.volmit.iris.util.data;

import com.volmit.iris.engine.object.IrisBiome;

public class BiomeMap {
    private final IrisBiome[] height;

    public BiomeMap() {
        height = new IrisBiome[256];
    }

    public void setBiome(int x, int z, IrisBiome h) {
        height[x * 16 + z] = h;
    }

    public IrisBiome getBiome(int x, int z) {
        return height[x * 16 + z];
    }
}
