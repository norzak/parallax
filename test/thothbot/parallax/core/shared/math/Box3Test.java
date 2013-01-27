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

package thothbot.parallax.core.shared.math;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;

public class Box3Test extends GWTTestCase
{

	private static Vector3 negInf3 = new Vector3( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY );
	private static Vector3 posInf3 = new Vector3( Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY );

	private static Vector3 zero3 = new Vector3();
	private static Vector3 one3 = new Vector3( 1, 1, 1 );
	private static Vector3 two3 = new Vector3( 2, 2, 2 );

	
	@Override
	public String getModuleName() {
		return "thothbot.parallax.core.Core";
	}
	
	@Test
	public void testBox3() 
	{
		Box3 a = new Box3();
		assertTrue( a.getMin().equals( posInf3 ));
		assertTrue( a.getMax().equals( negInf3 ));

		Box3 b = new Box3( zero3.clone(), zero3.clone() );
		assertTrue( b.getMin().equals( zero3 ));
		assertTrue( b.getMax().equals( zero3 ));

		Box3 c = new Box3( zero3.clone(), one3.clone() );
		assertTrue( c.getMin().equals( zero3 ));
		assertTrue( c.getMax().equals( one3 ));
	}

	@Test
	public void testSet() 
	{
		Box3 a = new Box3();

		a.set( zero3, one3 );
		assertTrue( a.getMin().equals( zero3 ));
		assertTrue( a.getMax().equals( one3 ));
	}

	@Test
	public void testCopy() 
	{
		Box3 a = new Box3( zero3.clone(), one3.clone() );
		Box3 b = new Box3().copy( a );
		assertTrue( b.getMin().equals( zero3 ));
		assertTrue( b.getMax().equals( one3 ));

		// ensure that it is a true copy
		a.setMin( zero3 );
		a.setMax( one3 );
		assertTrue( b.getMin().equals( zero3 ));
		assertTrue( b.getMax().equals( one3 ));
	}

	@Test
	public void testMakeEmpty() 
	{
		Box3 a = new Box3();

		assertTrue( a.isEmpty());

		Box3 b = new Box3( zero3.clone(), one3.clone() );
		assertTrue( ! b.isEmpty());

		b.makeEmpty();
		assertTrue( b.isEmpty());
	}

	@Test
	public void testCenter() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		assertTrue( a.center().equals( zero3 ));

		a = new Box3( zero3.clone(), one3.clone() );
		Vector3 midpoint = one3.clone().multiply( 0.5 );
		assertTrue( a.center().equals( midpoint ));
	}

	@Test
	public void testSize() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		assertTrue( a.size().equals( zero3 ));

		a = new Box3( zero3.clone(), one3.clone() );
		assertTrue( a.size().equals( one3 ));
	}

	@Test
	public void testExpandByPoint() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		a.expandByPoint( zero3 );
		assertTrue( a.size().equals( zero3 ));

		a.expandByPoint( one3 );
		assertTrue( a.size().equals( one3 ));

		a.expandByPoint( one3.clone().negate() );
		assertTrue( a.size().equals( one3.clone().multiply( 2 ) ));
		assertTrue( a.center().equals( zero3 ));
	}

	@Test
	public void testExpandByVector() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		a.expandByVector( zero3 );
		assertTrue( a.size().equals( zero3 ));

		a.expandByVector( one3 );
		assertTrue( a.size().equals( one3.clone().multiply( 2 ) ));
		assertTrue( a.center().equals( zero3 ));
	}

	@Test
	public void testExpandByScalar() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		a.expandByScalar( 0 );
		assertTrue( a.size().equals( zero3 ));

		a.expandByScalar( 1 );
		assertTrue( a.size().equals( one3.clone().multiply( 2 ) ));
		assertTrue( a.center().equals( zero3 ));
	}

	@Test
	public void testIsContainsPoint() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );

		assertTrue( a.isContainsPoint( zero3 ));
		assertTrue( ! a.isContainsPoint( one3 ));

		a.expandByScalar( 1 );
		assertTrue( a.isContainsPoint( zero3 ));
		assertTrue( a.isContainsPoint( one3 ));
		assertTrue( a.isContainsPoint( one3.clone().negate() ));
	}

	@Test
	public void testIsContainsBox() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.isContainsBox( a ));
		assertTrue( ! a.isContainsBox( b ));
		assertTrue( ! a.isContainsBox( c ));

		assertTrue( b.isContainsBox( a ));
		assertTrue( c.isContainsBox( a ));
		assertTrue( ! b.isContainsBox( c ));
	}

	@Test
	public void testGetParameter() 
	{
		Box3 a = new Box3( zero3.clone(), one3.clone() );
		Box3 b = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.getParameter( new Vector3( 0, 0, 0 ) ).equals( new Vector3( 0, 0, 0 ) ));
		assertTrue( a.getParameter( new Vector3( 1, 1, 1 ) ).equals( new Vector3( 1, 1, 1 ) ));

		assertTrue( b.getParameter( new Vector3( -1, -1, -1 ) ).equals( new Vector3( 0, 0, 0 ) ));
		assertTrue( b.getParameter( new Vector3( 0, 0, 0 ) ).equals( new Vector3( 0.5, 0.5, 0.5 ) ));
		assertTrue( b.getParameter( new Vector3( 1, 1, 1 ) ).equals( new Vector3( 1, 1, 1 ) ));
	}

	@Test
	public void testIsIntersectionBox() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.isIntersectionBox( a ));
		assertTrue( a.isIntersectionBox( b ));
		assertTrue( a.isIntersectionBox( c ));

		assertTrue( b.isIntersectionBox( a ));
		assertTrue( c.isIntersectionBox( a ));
		assertTrue( b.isIntersectionBox( c ));

		b.translate( new Vector3( 2, 2, 2 ) );
		assertTrue( ! a.isIntersectionBox( b ));
		assertTrue( ! b.isIntersectionBox( a ));
		assertTrue( ! b.isIntersectionBox( c ));
	}

	@Test
	public void testClampPoint() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.clampPoint( new Vector3( 0, 0, 0 ) ).equals( new Vector3( 0, 0, 0 ) ));
		assertTrue( a.clampPoint( new Vector3( 1, 1, 1 ) ).equals( new Vector3( 0, 0, 0 ) ));
		assertTrue( a.clampPoint( new Vector3( -1, -1, -1 ) ).equals( new Vector3( 0, 0, 0 ) ));

		assertTrue( b.clampPoint( new Vector3( 2, 2, 2 ) ).equals( new Vector3( 1, 1, 1 ) ));
		assertTrue( b.clampPoint( new Vector3( 1, 1, 1 ) ).equals( new Vector3( 1, 1, 1 ) ));
		assertTrue( b.clampPoint( new Vector3( 0, 0, 0 ) ).equals( new Vector3( 0, 0, 0 ) ));
		assertTrue( b.clampPoint( new Vector3( -1, -1, -1 ) ).equals( new Vector3( -1, -1, -1 ) ));
		assertTrue( b.clampPoint( new Vector3( -2, -2, -2 ) ).equals( new Vector3( -1, -1, -1 ) ));
	}

	@Test
	public void testDistanceToPoint() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.distanceToPoint( new Vector3( 0, 0, 0 ) ) == 0);
		assertTrue( a.distanceToPoint( new Vector3( 1, 1, 1 ) ) == Math.sqrt( 3 ));
		assertTrue( a.distanceToPoint( new Vector3( -1, -1, -1 ) ) == Math.sqrt( 3 ));

		assertTrue( b.distanceToPoint( new Vector3( 2, 2, 2 ) ) == Math.sqrt( 3 ));
		assertTrue( b.distanceToPoint( new Vector3( 1, 1, 1 ) ) == 0);
		assertTrue( b.distanceToPoint( new Vector3( 0, 0, 0 ) ) == 0);
		assertTrue( b.distanceToPoint( new Vector3( -1, -1, -1 ) ) == 0);
		assertTrue( b.distanceToPoint( new Vector3( -2, -2, -2 ) ) == Math.sqrt( 3 ));
	}

	@Test
	public void testGetBoundingSphere() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.getBoundingSphere().equals( new Sphere( zero3, 0 ) ));
		assertTrue( b.getBoundingSphere().equals( new Sphere( one3.clone().multiply( 0.5 ), Math.sqrt( 3 ) * 0.5 ) ));
		assertTrue( c.getBoundingSphere().equals( new Sphere( zero3, Math.sqrt( 12 ) * 0.5 ) ));
	}

	@Test
	public void testIntersect() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.clone().intersect( a ).equals( a ));
		assertTrue( a.clone().intersect( b ).equals( a ));
		assertTrue( b.clone().intersect( b ).equals( b ));
		assertTrue( a.clone().intersect( c ).equals( a ));
		assertTrue( b.clone().intersect( c ).equals( b ));
		assertTrue( c.clone().intersect( c ).equals( c ));
	}

	@Test
	public void testUnion() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );

		assertTrue( a.clone().union( a ).equals( a ));
		assertTrue( a.clone().union( b ).equals( b ));
		assertTrue( a.clone().union( c ).equals( c ));
		assertTrue( b.clone().union( c ).equals( c ));
	}

	@Test
	public void testTransform() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );
		Box3 d = new Box3( one3.clone().negate(), zero3.clone() );

		Matrix4 m = new Matrix4().makeTranslation( 1, -2, 1 );
		Vector3 t1 = new Vector3( 1, -2, 1 );

		assertTrue( compareBox( a.clone().transform( m ), a.clone().translate( t1 ) ));
		assertTrue( compareBox( b.clone().transform( m ), b.clone().translate( t1 ) ));
		assertTrue( compareBox( c.clone().transform( m ), c.clone().translate( t1 ) ));
		assertTrue( compareBox( d.clone().transform( m ), d.clone().translate( t1 ) ));
	}
	
	private boolean compareBox( Box3 a, Box3 b ) 
	{
		double threshold = 0.0001;
		return ( a.getMin().distanceTo( b.getMin() ) < threshold &&
		a.getMax().distanceTo( b.getMax() ) < threshold );
	};

	@Test
	public void testTranslate() 
	{
		Box3 a = new Box3( zero3.clone(), zero3.clone() );
		Box3 b = new Box3( zero3.clone(), one3.clone() );
		Box3 c = new Box3( one3.clone().negate(), one3.clone() );
		Box3 d = new Box3( one3.clone().negate(), zero3.clone() );

		assertTrue( a.clone().translate( one3 ).equals( new Box3( one3, one3 ) ));
		assertTrue( a.clone().translate( one3 ).translate( one3.clone().negate() ).equals( a ));
		assertTrue( d.clone().translate( one3 ).equals( b ));
		assertTrue( b.clone().translate( one3.clone().negate() ).equals( d ));
	}

}