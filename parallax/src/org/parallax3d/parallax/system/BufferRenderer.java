/*
 * Copyright 2016 Alex Usachev, thothbot@gmail.com
 *
 * This file is part of Parallax project.
 *
 * Parallax is free software: you can redistribute it and/or modify it
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 *
 * Parallax is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution
 * 3.0 Unported License. for more details.
 *
 * You should have received a copy of the the Creative Commons Attribution
 * 3.0 Unported License along with Parallax.
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */
package org.parallax3d.parallax.system;

import org.parallax3d.parallax.graphics.core.InstancedBufferGeometry;
import org.parallax3d.parallax.system.gl.enums.BeginMode;

public abstract class BufferRenderer {

    BeginMode mode;

    public void setMode( BeginMode value ) {

        mode = value;

    }

    public abstract void renderInstances(InstancedBufferGeometry geometry, int drawStart, int drawCount);
    public abstract void render(int drawStart, int drawCount);
}
