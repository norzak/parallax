/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
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

package org.parallax3d.parallax.graphics.renderers.shaders;

import java.util.Arrays;
import java.util.List;

import org.parallax3d.parallax.App;
import org.parallax3d.parallax.system.ClassUtils;
import org.parallax3d.parallax.system.SourceTextResource;

/**
 * CubeGeometry map shader.
 * <p>
 * Based on three.js code.
 *  
 * @author thothbot
 *
 */
public final class CubeShader extends Shader 
{
	interface Resources extends DefaultResources
	{
		Resources INSTANCE = ClassUtils.newProxyInstance(Resources.class);

		@Source("source/cube.vs.glsl")
		SourceTextResource getVertexShader();

		@Source("source/cube.fs.glsl")
		SourceTextResource getFragmentShader();
	}

	public CubeShader()
	{
		super(Resources.INSTANCE);
	}

	@Override
	protected void initUniforms()
	{
		this.addUniform("tCube", new Uniform(Uniform.TYPE.T ));
		this.addUniform("tFlip", new Uniform(Uniform.TYPE.F, -1.0f ));
	}
	
	@Override
	protected void updateVertexSource(String src)
	{
		List<String> vars = Arrays.asList(
			ChunksVertexShader.LOGDEPTHBUF_PAR
		);
		
		List<String> main = Arrays.asList(
			ChunksVertexShader.LOGDEPTHBUF
		);

		super.updateVertexSource(updateShaderSource(src, vars, main));
	}
	
	@Override
	protected void updateFragmentSource(String src)
	{
		List<String> vars = Arrays.asList(
			ChunksFragmentShader.LOGDEPTHBUF_PAR
		);
			
		List<String> main = Arrays.asList(
			ChunksFragmentShader.LOGDEPTHBUF
		);
			
		super.updateFragmentSource(updateShaderSource(src, vars, main));
	}
}
