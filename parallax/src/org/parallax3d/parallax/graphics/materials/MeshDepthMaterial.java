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

package org.parallax3d.parallax.graphics.materials;

import org.parallax3d.parallax.graphics.cameras.Camera;
import org.parallax3d.parallax.graphics.cameras.HasNearFar;
import org.parallax3d.parallax.graphics.renderers.shaders.DepthShader;
import org.parallax3d.parallax.graphics.renderers.shaders.Shader;
import org.parallax3d.parallax.graphics.renderers.shaders.Uniform;
import org.parallax3d.parallax.system.FastMap;
import org.parallax3d.parallax.system.ThreejsObject;

/**
 * A material for drawing geometry by depth. Depth is based off of the camera near and far plane. White is nearest, black is farthest.
 *
 */
@ThreejsObject("THREE.MeshDepthMaterial")
public class MeshDepthMaterial extends Material implements HasWireframe
{
	//	private boolean isMorphTargets = false;
	private boolean isWireframe;
	private int wireframeLineWidth;

	public MeshDepthMaterial()
	{
		setWireframe(false);
		setWireframeLineWidth(1);
	}

	public Material.SHADING bufferGuessNormalType ()
	{
		// only MeshBasicMaterial and MeshDepthMaterial don't need normals
		return null;
	}

	@Override
	public Shader getAssociatedShader()
	{
		return new DepthShader();
	}

	@Override
	public boolean isWireframe() {
		return this.isWireframe;
	}

	@Override
	public MeshDepthMaterial setWireframe(boolean wireframe) {
		this.isWireframe = wireframe;
		return this;
	}

	@Override
	public int getWireframeLineWidth() {
		return this.wireframeLineWidth;
	}

	@Override
	public MeshDepthMaterial setWireframeLineWidth(int wireframeLineWidth) {
		this.wireframeLineWidth = wireframeLineWidth;
		return this;
	}

	@Override
	public MeshDepthMaterial clone () {

		MeshDepthMaterial material = new MeshDepthMaterial();

		super.clone(material);

		material.isWireframe = this.isWireframe;
		material.wireframeLineWidth = this.wireframeLineWidth;

		return material;

	}

	@Override
	public void refreshUniforms(Camera camera, boolean isGammaInput)
	{
		super.refreshUniforms(camera, isGammaInput);
		FastMap<Uniform> uniforms = getShader().getUniforms();

		if(camera instanceof HasNearFar)
		{
			uniforms.get("mNear").setValue( ((HasNearFar) camera).getNear() );
			uniforms.get("mFar").setValue( ((HasNearFar) camera).getFar() );
		}

		uniforms.get("opacity").setValue( getOpacity() );
	}
}
