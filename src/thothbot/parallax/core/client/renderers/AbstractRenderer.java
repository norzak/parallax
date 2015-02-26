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

package thothbot.parallax.core.client.renderers;

import thothbot.parallax.core.shared.math.Color;

public abstract class AbstractRenderer 
{
	// Default Color and alpha
	protected Color clearColor = new Color(0x000000);
	protected double clearAlpha = 1.0;

	public void setClearColor( int hex )
	{
		setClearColor(new Color(hex));
	}
	
	public void setClearColor( Color color )
	{
		setClearColor(color, 1.0);
	}

	/**
	 * Sets the the background color, using hex for the color.<br>
	 * 
	 * @param hex the clear color value.
	 */
	public void setClearColor( int hex, double alpha )
	{
		setClearColor(new Color(hex), alpha);
	}
	
	/**
	 * Sets the the background color, using {@link Color} for the color and alpha for the opacity.
	 * 
	 * @param color the {@link Color} instance.
	 * @param alpha the opacity of the scene's background color, range 0.0 (invisible) to 1.0 (opaque).
	 */
	public abstract void setClearColor( Color color, double alpha );	

	/**
	 * Returns the background color.
	 * 
	 * @return the {@link Color} instance. 
	 */
	public Color getClearColor() 
	{
		return this.clearColor;
	}

	/**
	 * Returns the opacity of the scene's background color, range 0.0 (invisible) to 1.0 (opaque)
	 * 
	 * @return the value in range &#60;0,1&#62;.
	 */
	public double getClearAlpha() 
	{
		return this.clearAlpha;
	}
}
