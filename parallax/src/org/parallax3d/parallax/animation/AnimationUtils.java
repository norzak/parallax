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
package org.parallax3d.parallax.animation;

import org.parallax3d.parallax.system.ThreejsObject;
import org.parallax3d.parallax.system.gl.arrays.Float32Array;

import java.util.List;

/**
 * @author tschw
 * @author Ben Houston / http://clara.io/
 * @author David Sarno / http://lighthaus.us/
 * @author thothbot
 */
@ThreejsObject("THREE.AnimationUtils")
public class AnimationUtils {

    // same as Array.prototype.slice, but also works on typed arrays
    public static Float32Array arraySlice(Float32Array array, int from, int to) {

        if ( THREE.AnimationUtils.isTypedArray( array ) ) {

            return new array.constructor( array.subarray( from, to ) );

        }

        return array.slice( from, to );

    }

    public static List<Object> arraySlice(List<Object> array, int from, int to) {

        if ( THREE.AnimationUtils.isTypedArray( array ) ) {

            return new array.constructor( array.subarray( from, to ) );

        }

        return array.slice( from, to );

    }

    // converts an array to a specific type
    public static void convertArray( array, type, forceClone ) {

        if ( ! array || // let 'undefined' and 'null' pass
                ! forceClone && array.constructor === type ) return array;

        if ( typeof type.BYTES_PER_ELEMENT === 'number' ) {

            return new type( array ); // create typed array

        }

        return Array.prototype.slice.call( array ); // create Array

    }

    public static boolean isTypedArray( object ) {

        return ArrayBuffer.isView( object ) &&
                ! ( object instanceof DataView );

    }

    // returns an array by which times and values can be sorted
    public static void getKeyframeOrder( times ) {

        function compareTime( i, j ) {

            return times[ i ] - times[ j ];

        }

        var n = times.length;
        var result = new Array( n );
        for ( var i = 0; i !== n; ++ i ) result[ i ] = i;

        result.sort( compareTime );

        return result;

    }

    // uses the array previously returned by 'getKeyframeOrder' to sort data
    public static void sortedArray( values, stride, order ) {

        var nValues = values.length;
        var result = new values.constructor( nValues );

        for ( var i = 0, dstOffset = 0; dstOffset !== nValues; ++ i ) {

            var srcOffset = order[ i ] * stride;

            for ( var j = 0; j !== stride; ++ j ) {

                result[ dstOffset ++ ] = values[ srcOffset + j ];

            }

        }

        return result;

    }

    // function for parsing AOS keyframe formats
    public static void flattenJSON( jsonKeys, times, values, valuePropertyName ) {

        var i = 1, key = jsonKeys[ 0 ];

        while ( key !== undefined && key[ valuePropertyName ] === undefined ) {

            key = jsonKeys[ i ++ ];

        }

        if ( key === undefined ) return; // no data

        var value = key[ valuePropertyName ];
        if ( value === undefined ) return; // no data

        if ( Array.isArray( value ) ) {

            do {

                value = key[ valuePropertyName ];

                if ( value !== undefined ) {

                    times.push( key.time );
                    values.push.apply( values, value ); // push all elements

                }

                key = jsonKeys[ i ++ ];

            } while ( key !== undefined );

        } else if ( value.toArray !== undefined ) {
            // ...assume THREE.Math-ish

            do {

                value = key[ valuePropertyName ];

                if ( value !== undefined ) {

                    times.push( key.time );
                    value.toArray( values, values.length );

                }

                key = jsonKeys[ i ++ ];

            } while ( key !== undefined );

        } else {
            // otherwise push as-is

            do {

                value = key[ valuePropertyName ];

                if ( value !== undefined ) {

                    times.push( key.time );
                    values.push( value );

                }

                key = jsonKeys[ i ++ ];

            } while ( key !== undefined );

        }

    }
}