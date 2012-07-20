/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Parallax. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.core.client.gl2.enums;

/**
 * Implements the wrap parameter for texture coordinate.
 * It is useful for preventing wrapping artifacts when mapping 
 * a single image onto an object.
 * 
 * @author thothbot
 *
 */
public enum TextureWrapMode 
{
	/**
	 *  This causes the integer part of the s coordinate to be ignored; 
	 *  the GL uses only the fractional part, thereby creating a 
	 *  repeating pattern. 
	 */
	REPEAT(GLenum.REPEAT),
	/**
	 * This causes s or t coordinates to be clamped to the range [0, 1] 
	 * and is useful for preventing wrapping artifacts when mapping a 
	 * single image onto an object.
	 */
	CLAMP_TO_EDGE(GLenum.CLAMP_TO_EDGE),
	MIRRORED_REPEAT(GLenum.MIRRORED_REPEAT);

	private final int value;

	private TextureWrapMode(GLenum GLenum) 
	{
		this.value = GLenum.getValue();
	}

	/**
	 * Gets the enum's numerical value.
	 */
	public int getValue() 
	{
		return value;
	}
}