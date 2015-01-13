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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thothbot.parallax.core.client.context.Canvas3d;
import thothbot.parallax.core.client.events.HasEventBus;
import thothbot.parallax.core.client.events.ViewportResizeEvent;
import thothbot.parallax.core.client.gl2.WebGLConstants;
import thothbot.parallax.core.client.gl2.WebGLFramebuffer;
import thothbot.parallax.core.client.gl2.WebGLProgram;
import thothbot.parallax.core.client.gl2.WebGLRenderingContext;
import thothbot.parallax.core.client.gl2.WebGLUniformLocation;
import thothbot.parallax.core.client.gl2.arrays.Float32Array;
import thothbot.parallax.core.client.gl2.arrays.Int16Array;
import thothbot.parallax.core.client.gl2.arrays.Uint16Array;
import thothbot.parallax.core.client.gl2.arrays.Uint8Array;
import thothbot.parallax.core.client.gl2.enums.BeginMode;
import thothbot.parallax.core.client.gl2.enums.BlendEquationMode;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorDest;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorSrc;
import thothbot.parallax.core.client.gl2.enums.BufferTarget;
import thothbot.parallax.core.client.gl2.enums.ClearBufferMask;
import thothbot.parallax.core.client.gl2.enums.CullFaceMode;
import thothbot.parallax.core.client.gl2.enums.DataType;
import thothbot.parallax.core.client.gl2.enums.DepthFunction;
import thothbot.parallax.core.client.gl2.enums.DrawElementsType;
import thothbot.parallax.core.client.gl2.enums.EnableCap;
import thothbot.parallax.core.client.gl2.enums.FrontFaceDirection;
import thothbot.parallax.core.client.gl2.enums.PixelStoreParameter;
import thothbot.parallax.core.client.gl2.enums.TextureMinFilter;
import thothbot.parallax.core.client.gl2.enums.TextureTarget;
import thothbot.parallax.core.client.gl2.enums.TextureUnit;
import thothbot.parallax.core.client.gl2.extension.ExtTextureFilterAnisotropic;
import thothbot.parallax.core.client.gl2.extension.OESStandardDerivatives;
import thothbot.parallax.core.client.gl2.extension.OESTextureFloat;
import thothbot.parallax.core.client.gl2.extension.WebGLCompressedTextureS3tc;
import thothbot.parallax.core.client.renderers.WebGLExtensions.Id;
import thothbot.parallax.core.client.shaders.Attribute;
import thothbot.parallax.core.client.shaders.ProgramParameters;
import thothbot.parallax.core.client.shaders.Shader;
import thothbot.parallax.core.client.shaders.Uniform;
import thothbot.parallax.core.client.shaders.Uniform.TYPE;
import thothbot.parallax.core.client.textures.CompressedTexture;
import thothbot.parallax.core.client.textures.CubeTexture;
import thothbot.parallax.core.client.textures.DataTexture;
import thothbot.parallax.core.client.textures.RenderTargetCubeTexture;
import thothbot.parallax.core.client.textures.RenderTargetTexture;
import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.cameras.Camera;
import thothbot.parallax.core.shared.core.AbstractGeometry;
import thothbot.parallax.core.shared.core.BufferAttribute;
import thothbot.parallax.core.shared.core.BufferGeometry.DrawCall;
import thothbot.parallax.core.shared.core.FastMap;
import thothbot.parallax.core.shared.core.BufferGeometry;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.GeometryObject;
import thothbot.parallax.core.shared.core.Object3D;
import thothbot.parallax.core.shared.lights.DirectionalLight;
import thothbot.parallax.core.shared.lights.HemisphereLight;
import thothbot.parallax.core.shared.lights.Light;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.lights.ShadowLight;
import thothbot.parallax.core.shared.lights.SpotLight;
import thothbot.parallax.core.shared.materials.HasEnvMap;
import thothbot.parallax.core.shared.materials.HasFog;
import thothbot.parallax.core.shared.materials.HasSkinning;
import thothbot.parallax.core.shared.materials.HasWireframe;
import thothbot.parallax.core.shared.materials.LineBasicMaterial;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;
import thothbot.parallax.core.shared.materials.ShaderMaterial;
import thothbot.parallax.core.shared.math.Color;
import thothbot.parallax.core.shared.math.Frustum;
import thothbot.parallax.core.shared.math.Mathematics;
import thothbot.parallax.core.shared.math.Matrix4;
import thothbot.parallax.core.shared.math.Vector2;
import thothbot.parallax.core.shared.math.Vector3;
import thothbot.parallax.core.shared.math.Vector4;
import thothbot.parallax.core.shared.objects.Line;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.objects.PointCloud;
import thothbot.parallax.core.shared.scenes.AbstractFog;
import thothbot.parallax.core.shared.scenes.Fog;
import thothbot.parallax.core.shared.scenes.FogExp2;
import thothbot.parallax.core.shared.scenes.Scene;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;

/**
 * The WebGL renderer displays your beautifully crafted {@link Scene}s using WebGL, if your device supports it.
 */
public class WebGLRenderer implements HasEventBus
{
	// The HTML5 Canvas's 'webgl' context obtained from the canvas where the renderer will draw.
	private WebGLRenderingContext gl;

	private WebGlRendererInfo info;
				
	// Integer, default is Color(0x000000).
	private Color clearColor = new Color(0x000000);

	// double, default is 0
	private double clearAlpha = 1.0;
	
	// Integer, default is 4
	private int maxLights = 4;
	
	// Properties
	private boolean isAutoClear = true;
	private boolean isAutoClearColor = true;
	private boolean isAutoClearDepth = true;
	private boolean isAutoClearStencil = true;

	// scene graph
	private boolean isSortObjects = true;
	private boolean isAutoUpdateObjects = true;
	private boolean isAutoUpdateScene = true;

	// physically based shading
	private boolean isGammaInput = false;
	private boolean isGammaOutput = false;
	private boolean isPhysicallyBasedShading = false;

	// morphs
	private int maxMorphTargets = 8;
	private int maxMorphNormals = 4;

	// flags
	private boolean isAutoScaleCubemaps = true;

	// custom render plugins

	// An array with render plugins to be applied before rendering.
	private List<Plugin> renderPluginsPre;
	
	//An array with render plugins to be applied after rendering.
	private List<Plugin> renderPluginsPost;

	// internal state cache

	private WebGLProgram _currentProgram = null;
	private WebGLFramebuffer _currentFramebuffer = null;
	private int _currentMaterialId = -1;
	private int _currentGeometryGroupHash = -1;
	private Camera _currentCamera = null;
	
	private int _usedTextureUnits = 0;
	
	// GL state cache

	private Material.SIDE cache_oldMaterialSided = null;

	private Material.BLENDING cache_oldBlending = null;
	private BlendEquationMode cache_oldBlendEquation = null;

	private BlendingFactorSrc cache_oldBlendSrc = null;
	private BlendingFactorDest cache_oldBlendDst = null;

	private Boolean cache_oldDepthTest = null;
	private Boolean cache_oldDepthWrite = null;

	private Boolean cache_oldPolygonOffset = null;
	private Double cache_oldPolygonOffsetFactor = null;
	private Double cache_oldPolygonOffsetUnits = null;
			
	private int absoluteWidth = 0;
	private int absoluteHeight = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	private int _currentWidth = 0;
	private int _currentHeight = 0;
	
	// frustum

	private Frustum _frustum = new Frustum();

	 // camera matrices cache

	private Matrix4 _projScreenMatrix = new Matrix4();
	private Matrix4 _projScreenMatrixPS = new Matrix4();
	
	private Vector3 _vector3 = new Vector3();

	// light arrays cache
	private boolean _lightsNeedUpdate = true;
	private RendererLights cache_lights;
	
	public Map<String, Shader> _programs;
	
	private List<Light> lights = new ArrayList<Light>();
	private List<RendererObject> opaqueObjects = new ArrayList<RendererObject>();
	private List<RendererObject> transparentObjects = new ArrayList<RendererObject>();

//	var sprites = [];
//	var lensFlares = [];

	public List<RendererObject> _webglObjects = new ArrayList<RendererObject>();	
	public List<RendererObject> _webglObjectsImmediate  = new ArrayList<RendererObject>();

	// GPU capabilities
	private int GPUmaxTextures;
	private int GPUmaxVertexTextures;
	private int GPUmaxTextureSize;
	private int GPUmaxCubemapSize;
	private int GPUmaxAnisotropy;

	private boolean isGPUsupportsVertexTextures;
	private boolean isGPUsupportsBoneTextures;
		
	private OESTextureFloat GLExtensionTextureFloat;
	private OESStandardDerivatives GLExtensionStandardDerivatives;
	private ExtTextureFilterAnisotropic GLExtensionTextureFilterAnisotropic;
	private WebGLCompressedTextureS3tc GLExtensionCompressedTextureS3TC;
	
	private Uint8Array _newAttributes = Uint8Array.create( 16 );
	private Uint8Array _enabledAttributes = Uint8Array.create( 16 );
	
	/**
	 * The constructor will create renderer for the {@link Canvas3d} widget.
	 * 
	 * @param gl     the {@link WebGLRenderingContext}
	 * @param width  the viewport width
	 * @param height the viewport height
	 */
	public WebGLRenderer(WebGLRenderingContext gl, int width, int height)
	{
		this.gl = gl;

		this.setInfo(new WebGlRendererInfo());
		
		this.cache_lights           = new RendererLights();
		this._programs         = GWT.isScript() ? 
				new FastMap<Shader>() : new HashMap<String, Shader>();
			
		this.GPUmaxTextures       = gl.getParameteri(WebGLConstants.MAX_TEXTURE_IMAGE_UNITS);
		this.GPUmaxVertexTextures = gl.getParameteri(WebGLConstants.MAX_VERTEX_TEXTURE_IMAGE_UNITS);
		this.GPUmaxTextureSize    = gl.getParameteri(WebGLConstants.MAX_TEXTURE_SIZE);
		this.GPUmaxCubemapSize    = gl.getParameteri(WebGLConstants.MAX_CUBE_MAP_TEXTURE_SIZE);

		this.isGPUsupportsVertexTextures = ( this.GPUmaxVertexTextures > 0 ); 
		this.isGPUsupportsBoneTextures = this.isGPUsupportsVertexTextures && WebGLExtensions.get(gl, WebGLExtensions.Id.OES_texture_float) != null ;

		//

//		var _vertexShaderPrecisionHighpFloat = gl.getShaderPrecisionFormat( gl.VERTEX_SHADER, gl.HIGH_FLOAT );
//		var _vertexShaderPrecisionMediumpFloat = gl.getShaderPrecisionFormat( gl.VERTEX_SHADER, gl.MEDIUM_FLOAT );
//		var _vertexShaderPrecisionLowpFloat = gl.getShaderPrecisionFormat( gl.VERTEX_SHADER, gl.LOW_FLOAT );
//
//		var _fragmentShaderPrecisionHighpFloat = gl.getShaderPrecisionFormat( gl.FRAGMENT_SHADER, gl.HIGH_FLOAT );
//		var _fragmentShaderPrecisionMediumpFloat = gl.getShaderPrecisionFormat( gl.FRAGMENT_SHADER, gl.MEDIUM_FLOAT );
//		var _fragmentShaderPrecisionLowpFloat = gl.getShaderPrecisionFormat( gl.FRAGMENT_SHADER, gl.LOW_FLOAT );

		
		this.GLExtensionTextureFloat = (OESTextureFloat) WebGLExtensions.get(gl, WebGLExtensions.Id.OES_texture_float);
		WebGLExtensions.get(gl, WebGLExtensions.Id.OES_texture_float_linear);
		WebGLExtensions.get(gl, WebGLExtensions.Id.OES_standard_derivatives);
		
//		this.GLExtensionStandardDerivatives = (OESStandardDerivatives) gl.getExtension( "OES_standard_derivatives" );
//		if(this.GLExtensionStandardDerivatives == null)
//			Log.warn( "WebGLRenderer: Standard derivatives not supported." );
//
//		this.GLExtensionTextureFilterAnisotropic = (ExtTextureFilterAnisotropic) gl.getExtension( "EXT_texture_filter_anisotropic" );
//		if(this.GLExtensionTextureFilterAnisotropic == null)
//			this.GLExtensionTextureFilterAnisotropic = (ExtTextureFilterAnisotropic) gl.getExtension( "MOZ_EXT_texture_filter_anisotropic" );
//		if(this.GLExtensionTextureFilterAnisotropic == null)
//			this.GLExtensionTextureFilterAnisotropic = (ExtTextureFilterAnisotropic) gl.getExtension( "WEBKIT_EXT_texture_filter_anisotropic" );
//		if(this.GLExtensionTextureFilterAnisotropic == null)
//			Log.warn( "WebGLRenderer: Anisotropic texture filtering not supported." );
//		else
//			this.GPUmaxAnisotropy = getGL().getParameteri(ExtTextureFilterAnisotropic.MAX_TEXTURE_MAX_ANISOTROPY_EXT);	
//		
//		this.GLExtensionCompressedTextureS3TC = (WebGLCompressedTextureS3tc) gl.getExtension( "WEBGL_compressed_texture_s3tc" );
//		if(this.GLExtensionCompressedTextureS3TC == null)
//			this.GLExtensionCompressedTextureS3TC = (WebGLCompressedTextureS3tc) gl.getExtension( "MOZ_WEBGL_compressed_texture_s3tc" );
//		if(this.GLExtensionCompressedTextureS3TC == null)
//			this.GLExtensionCompressedTextureS3TC = (WebGLCompressedTextureS3tc) gl.getExtension( "WEBKIT_WEBGL_compressed_texture_s3tc" );
//		if(this.GLExtensionCompressedTextureS3TC == null)
//			Log.warn( "WebGLRenderer: S3TC compressed textures not supported." );


		setSize(width, height);
		setDefaultGLState();
		
		// default plugins (order is important)
		this.renderPluginsPre = new ArrayList<Plugin>();
		this.renderPluginsPost = new ArrayList<Plugin>();
	}

	public void addPlugin(Plugin plugin)
	{
		if(plugin.getType() == Plugin.TYPE.PRE_RENDER)
		{
			this.renderPluginsPre.add( plugin );
		}
		else if(plugin.getType() == Plugin.TYPE.POST_RENDER)
		{
			this.renderPluginsPost.add( plugin );
		}
		else
		{
			Log.error("Unknown plugin type: " + plugin.getType());
			return;
		}
	}
	
	public int getGPUmaxTextures() {
		return GPUmaxTextures;
	}

	public int getGPUmaxVertexTextures() {
		return GPUmaxVertexTextures;
	}

	public int getGPUmaxTextureSize() {
		return GPUmaxTextureSize;
	}

	public int getGPUmaxCubemapSize() {
		return GPUmaxCubemapSize;
	}

	public int getGPUmaxAnisotropy() {
		return GPUmaxAnisotropy;
	}

	/**
	 * Gets {@link #setAutoClear(boolean)} flag.
	 */
	public boolean isAutoClear() {
		return isAutoClear;
	}

	/**
	 * Defines whether the renderer should automatically clear its output before rendering.
	 * Default is true.
	 * 
	 * @param isAutoClear false or true
	 */
	public void setAutoClear(boolean isAutoClear) {
		this.isAutoClear = isAutoClear;
	}

	/**
	 * Gets {@link #setAutoClearColor(boolean)} flag.
	 */
	public boolean isAutoClearColor() {
		return isAutoClearColor;
	}

	/**
	 * Defines whether the renderer should clear the color buffer. 
	 * Default is true.
	 * 
	 * @param isAutoClearColor false or true
	 */
	public void setAutoClearColor(boolean isAutoClearColor) {
		this.isAutoClearColor = isAutoClearColor;
	}


	/**
	 * Gets {@link #setAutoClearDepth(boolean)} flag.
	 */
	public boolean isAutoClearDepth() {
		return isAutoClearDepth;
	}

	/**
	 * Defines whether the renderer should clear the depth buffer. 
	 * Default is true.
	 * 
	 * @param isAutoClearDepth false or true
	 */
	public void setAutoClearDepth(boolean isAutoClearDepth) {
		this.isAutoClearDepth = isAutoClearDepth;
	}

	/**
	 * Gets {@link #setAutoClearStencil(boolean)} flag.
	 */
	public boolean isAutoClearStencil() {
		return isAutoClearStencil;
	}

	/**
	 * Defines whether the renderer should clear the stencil buffer. 
	 * Default is true.
	 * 
	 * @param isAutoClearStencil false or true
	 */
	public void setAutoClearStencil(boolean isAutoClearStencil) {
		this.isAutoClearStencil = isAutoClearStencil;
	}

	/**
	 * Gets {@link #setSortObjects(boolean)} flag.
	 */
	public boolean isSortObjects() {
		return isSortObjects;
	}

	/**
	 * Defines whether the renderer should sort objects. 
	 * Default is true.
	 * 
	 * @param isSortObjects false or true
	 */
	public void setSortObjects(boolean isSortObjects) {
		this.isSortObjects = isSortObjects;
	}

	/**
	 * Gets {@link #setAutoUpdateObjects(boolean)} flag.
	 */
	public boolean isAutoUpdateObjects() {
		return isAutoUpdateObjects;
	}

	/**
	 * Defines whether the renderer should auto update objects. 
	 * Default is true.
	 * 
	 * @param isAutoUpdateObjects false or true
	 */
	public void setAutoUpdateObjects(boolean isAutoUpdateObjects) {
		this.isAutoUpdateObjects = isAutoUpdateObjects;
	}

	/**
	 * Gets {@link #setAutoUpdateScene(boolean)} flag.
	 */
	public boolean isAutoUpdateScene() {
		return isAutoUpdateScene;
	}
	
	public boolean isGammaInput() {
		return this.isGammaInput;
	}
	
	public void setGammaInput(boolean isGammaInput) {
		this.isGammaInput = isGammaInput;
	}
	
	public boolean isGammaOutput() {
		return this.isGammaOutput;
	}
	
	public void setGammaOutput(boolean isGammaOutput) {
		this.isGammaOutput = isGammaOutput;
	}

	public boolean isPhysicallyBasedShading() {
		return this.isPhysicallyBasedShading;
	}
	
	public void setPhysicallyBasedShading(boolean isPhysicallyBasedShading) {
		this.isPhysicallyBasedShading = isPhysicallyBasedShading;
	}

	/**
	 * Defines whether the renderer should auto update the scene.
	 * Default is true.
	 * 
	 * @param isAutoUpdateScene false or true
	 */
	public void setAutoUpdateScene(boolean isAutoUpdateScene) {
		this.isAutoUpdateScene = isAutoUpdateScene;
	}

	/**
	 * Gets {@link WebGlRendererInfo} instance with debug information.
	 * 
	 * @return the {@link WebGlRendererInfo} instance
	 */
	public WebGlRendererInfo getInfo() {
		return info;
	}
	
	private void setInfo(WebGlRendererInfo info) {
		this.info = info;
	}

	/**
	 * Gets the WebGL context from the {@link Canvas3d} widget.
	 * 
	 * @return the underlying context implementation for drawing onto the
	 *         {@link Canvas3d}.
	 */
	public WebGLRenderingContext getGL()
	{
		return this.gl;
	}

	private void setDefaultGLState () 
	{
		getGL().clearColor( 0.0, 0.0, 0.0, 1.0 );
		getGL().clearDepth( 1 );
		getGL().clearStencil( 0 );

		getGL().enable( EnableCap.DEPTH_TEST );
		getGL().depthFunc( DepthFunction.LEQUAL );

		getGL().frontFace( FrontFaceDirection.CCW );
		getGL().cullFace( CullFaceMode.BACK );
		getGL().enable( EnableCap.CULL_FACE );

		getGL().enable( EnableCap.BLEND );
		getGL().blendEquation( BlendEquationMode.FUNC_ADD );
		getGL().blendFunc( BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE_MINUS_SRC_ALPHA );
	}

	/**
	 * Return a Boolean true if the context supports vertex textures.
	 */
	public boolean supportsVertexTextures()
	{
		return this.GPUmaxVertexTextures > 0;
	}
	
	public boolean supportsFloatTextures() 
	{
		return WebGLExtensions.get( this.gl, Id.OES_texture_float ) != null;
	}

	public boolean supportsStandardDerivatives() 
	{
		return WebGLExtensions.get( this.gl, Id.OES_standard_derivatives ) != null;
	}

	public boolean supportsCompressedTextureS3TC() 
	{
		return WebGLExtensions.get( this.gl, Id.WEBGL_compressed_texture_s3tc ) != null;
	}

	public boolean supportsCompressedTexturePVRTC() 
	{
		return WebGLExtensions.get( this.gl, Id.WEBGL_compressed_texture_pvrtc ) != null;
	}

	public boolean supportsBlendMinMax() 
	{
		return WebGLExtensions.get( this.gl, Id.EXT_blend_minmax ) != null;
	}

	public int getMaxAnisotropy() 
	{
		if(this.GPUmaxAnisotropy > 0)
			return this.GPUmaxAnisotropy;

		if( WebGLExtensions.get( this.gl, Id.EXT_texture_filter_anisotropic ) != null)
			this.GPUmaxAnisotropy = getGL().getParameteri(ExtTextureFilterAnisotropic.MAX_TEXTURE_MAX_ANISOTROPY_EXT);

		return this.GPUmaxAnisotropy;
	}


	/**
	 * Sets the sizes and also sets {@link #setViewport(int, int, int, int)} size.
	 * 
	 * @param width  the {@link Canvas3d} width.
	 * @param height the {@link Canvas3d} height.
	 */
	public void setSize(int width, int height)
	{
		this.absoluteWidth = width;
		this.absoluteHeight = height;
		
		setViewport(0, 0, width, height);
		
		EVENT_BUS.fireEvent(new ViewportResizeEvent(this));
	}

	/**
	 * Sets the viewport to render from (X, Y) to (X + absoluteWidth, Y + absoluteHeight).
	 * By default X and Y = 0.
	 */
	public void setViewport(int x, int y, int width, int height)
	{
		this.viewportWidth = width;
		this.viewportHeight = height;

		getGL().viewport(x, y, this.viewportWidth, this.viewportHeight);
	}
	
	public int getAbsoluteWidth() {
		return this.absoluteWidth;
	}
	
	public int getAbsoluteHeight() {
		return this.absoluteHeight;
	}

	public double getAbsoluteAspectRation() {
		return getAbsoluteWidth() / (double)getAbsoluteHeight();
	}

	/**
	 * Sets the scissor area from (x, y) to (x + absoluteWidth, y + absoluteHeight).
	 */
	public void setScissor(int x, int y, int width, int height)
	{
		getGL().scissor(x, y, width, height);
	}

	/**
	 * Enable the scissor test. When this is enabled, only the pixels 
	 * within the defined scissor area will be affected by further 
	 * renderer actions.
	 */
	public void enableScissorTest(boolean enable)
	{
		if (enable)
			getGL().enable(EnableCap.SCISSOR_TEST);
		else
			getGL().disable(EnableCap.SCISSOR_TEST);
	}
	
	/**
	 * Specifies how many total lights are allowed in the scene 
	 * (divided evenly between point & directional lights) By default there are 4.
	 * 
	 * @param maxLights
	 */
	public void setMaxLights(int maxLights) 
	{
		this.maxLights = maxLights;
	}

	/**
	 * Sets the the background color, using hex for the color.<br>
	 * 
	 * @param hex the clear color value.
	 */
	public void setClearColor( int hex, double alpha  )
	{
		setClearColor(new Color(hex), alpha);
	}

	/**
	 * Sets the the background color, using {@link Color} for the color and alpha for the opacity.
	 * 
	 * @see #setClearColorHex(int, double). 
	 * 
	 * @param color the {@link Color} instance.
	 * @param alpha the opacity of the scene's background color, range 0.0 (invisible) to 1.0 (opaque).
	 */
	public void setClearColor( Color color, double alpha ) 
	{
		this.clearColor.copy(color);
		this.clearAlpha = alpha;

		getGL().clearColor( this.clearColor.getR(), this.clearColor.getG(), this.clearColor.getB(), this.clearAlpha );
	}

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
	 * @return the value in range <0,1>.
	 */
	public double getClearAlpha() 
	{
		return this.clearAlpha;
	}

	public void clear() 
	{
		clear(true, true, true);
	}

	/**
	 * Tells the renderer to clear its color, depth or stencil drawing buffer(s).
	 * If no parameters are passed, no buffer will be cleared.
	 * 
	 * @param color   is it should clear color
	 * @param depth   is it should clear depth
	 * @param stencil is it should clear stencil
	 */
	public void clear( boolean color, boolean depth, boolean stencil ) 
	{
		int bits = 0;

		if ( color ) bits |= ClearBufferMask.COLOR_BUFFER_BIT.getValue();
		if ( depth ) bits |= ClearBufferMask.DEPTH_BUFFER_BIT.getValue();
		if ( stencil ) bits |= ClearBufferMask.STENCIL_BUFFER_BIT.getValue();

		getGL().clear( bits );
	}
	
	public void clearColor() 
	{
		getGL().clear( ClearBufferMask.COLOR_BUFFER_BIT.getValue() );
	}

	public void clearDepth() 
	{
		getGL().clear( ClearBufferMask.DEPTH_BUFFER_BIT.getValue() );
	}

	public void clearStencil() 
	{
		getGL().clear( ClearBufferMask.STENCIL_BUFFER_BIT.getValue() );
	}


	/**
	 * Clear {@link RenderTargetTexture} and GL buffers.
	 * @see #clear(boolean, boolean, boolean).
	 */
	public void clearTarget( RenderTargetTexture renderTarget, boolean color, boolean depth, boolean stencil ) 
	{
		setRenderTarget( renderTarget );
		clear( color, depth, stencil );
	}
	
	private void initAttributes() {

		for ( int i = 0, l = _newAttributes.getLength(); i < l; i ++ ) {

			_newAttributes.set( i, 0);

		}

	}

	private void enableAttribute( Integer attribute ) {

		_newAttributes.set( attribute,  1);

		if ( _enabledAttributes.get( attribute ) == 0 ) {

			getGL().enableVertexAttribArray( attribute );
			_enabledAttributes.set( attribute, 1);

		}

	}

	private void  disableUnusedAttributes() {

		for ( int i = 0, l = _enabledAttributes.getLength(); i < l; i ++ ) {

			if ( _enabledAttributes.get( i ) != _newAttributes.get( i ) ) {

				getGL().disableVertexAttribArray( i );
				_enabledAttributes.set( i, 0);

			}

		}

	}

	
	/**
	 * Morph Targets Buffer initialization
	 */
	private void setupMorphTargets ( Material material, BufferGeometry geometrybuffer, Mesh object ) 
	{
		// set base
		Map<String, Integer> attributes = material.getShader().getAttributesLocations();
		Map<String, Uniform> uniforms = material.getShader().getUniforms();

		if ( object.morphTargetBase != - 1 && attributes.get("position") >= 0) 
		{
			getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglMorphTargetsBuffers.get( object.morphTargetBase ) );
			enableAttribute( attributes.get("position") );
			getGL().vertexAttribPointer( attributes.get("position"), 3, DataType.FLOAT, false, 0, 0 );

		} 
		else if ( attributes.get("position") >= 0 ) 
		{
			getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglVertexBuffer );
			enableAttribute( attributes.get("position") );
			getGL().vertexAttribPointer( attributes.get("position"), 3, DataType.FLOAT, false, 0, 0 );
		}

		if ( object.morphTargetForcedOrder.size() > 0 ) 
		{
			// set forced order

			int m = 0;
			List<Integer> order = object.morphTargetForcedOrder;
			List<Double> influences = object.morphTargetInfluences;

			while ( material instanceof ShaderMaterial 
					&& m < ((ShaderMaterial)material).getNumSupportedMorphTargets() 
					&& m < order.size() 
			) {
				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglMorphTargetsBuffers.get( order.get( m ) ) );
				enableAttribute( attributes.get("morphTarget" + m ) );
				getGL().vertexAttribPointer( attributes.get("morphTarget" + m ), 3, DataType.FLOAT, false, 0, 0 );

				if ( material instanceof HasSkinning && ((HasSkinning)material).isMorphNormals()) 
				{
					getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglMorphNormalsBuffers.get( order.get( m ) ) );
					enableAttribute(  attributes.get("morphNormal" + m ) );
					getGL().vertexAttribPointer( attributes.get("morphNormal" + m ), 3, DataType.FLOAT, false, 0, 0 );
				}

				object.__webglMorphTargetInfluences.set( m , influences.get( order.get( m ) ));

				m ++;
			}
		}
		else 
		{
			// find most influencing

			Map<Integer, Boolean> used = new HashMap<Integer, Boolean>();
			double candidateInfluence = - 1;
			int candidate = 0;
			List<Double> influences = object.morphTargetInfluences;			

			if ( object.morphTargetBase != - 1 )
				used.put( object.morphTargetBase, true);

			int m = 0;
			while ( material instanceof ShaderMaterial 
					&& m < ((ShaderMaterial)material).getNumSupportedMorphTargets() ) 
			{
				for ( int i = 0; i < influences.size(); i ++ ) 
				{
					if ( !used.containsKey(i) && influences.get( i ) > candidateInfluence ) 
					{
						candidate = i;
						candidateInfluence = influences.get( candidate );
					}
				}

				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglMorphTargetsBuffers.get( candidate ) );
				enableAttribute( attributes.get( "morphTarget" + m ) );
				getGL().vertexAttribPointer( attributes.get( "morphTarget" + m ), 3, DataType.FLOAT, false, 0, 0 );

				if ( material instanceof HasSkinning && ((HasSkinning)material).isMorphNormals() ) 
				{
					getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometrybuffer.__webglMorphNormalsBuffers.get( candidate ) );
					enableAttribute( attributes.get( "morphNormal" + m ) );
					getGL().vertexAttribPointer( attributes.get( "morphNormal" + m ), 3, DataType.FLOAT, false, 0, 0 );
				}

				object.__webglMorphTargetInfluences.set( m, candidateInfluence);

				used.put( candidate, true);
				candidateInfluence = -1;
				m ++;
			}
		}

		// load updated influences uniform
		if( uniforms.get("morphTargetInfluences").getLocation() != null ) 
		{
			Float32Array vals = object.__webglMorphTargetInfluences;
			double[] val2 = new double[vals.getLength()];
			for (int i = 0; i < vals.getLength(); i++) 
			{
			    Double f = vals.get(i);
			    val2[i] = (f != null ? f : Double.NaN); // Or whatever default you want.
			}
			getGL().uniform1fv( uniforms.get("morphTargetInfluences").getLocation(), val2 );
		}
	}
	
//	public void renderBufferImmediate( GeometryObject object, Shader program, Material material ) {
//
//		initAttributes();
//
//		if ( object.hasPositions && ! object.__webglVertexBuffer ) object.__webglVertexBuffer = getGL().createBuffer();
//		if ( object.hasNormals && ! object.__webglNormalBuffer ) object.__webglNormalBuffer = getGL().createBuffer();
//		if ( object.hasUvs && ! object.__webglUvBuffer ) object.__webglUvBuffer = getGL().createBuffer();
//		if ( object.hasColors && ! object.__webglColorBuffer ) object.__webglColorBuffer = getGL().createBuffer();
//
//		if ( object.hasPositions )
//		{
//
//			getGL().bindBuffer( getGL().ARRAY_BUFFER, object.__webglVertexBuffer );
//			getGL().bufferData( getGL().ARRAY_BUFFER, object.positionArray, getGL().DYNAMIC_DRAW );
//			enableAttribute( program.attributes.position );
//			getGL().vertexAttribPointer( program.attributes.position, 3, getGL().FLOAT, false, 0, 0 );
//
//		}
//
//		if ( object.hasNormals ) {
//
//			getGL().bindBuffer( getGL().ARRAY_BUFFER, object.__webglNormalBuffer );
//
//			if ( material.shading === THREE.FlatShading ) {
//
//				var nx, ny, nz,
//					nax, nbx, ncx, nay, nby, ncy, naz, nbz, ncz,
//					normalArray,
//					i, il = object.count * 3;
//
//				for ( i = 0; i < il; i += 9 ) {
//
//					normalArray = object.normalArray;
//
//					nax  = normalArray[ i ];
//					nay  = normalArray[ i + 1 ];
//					naz  = normalArray[ i + 2 ];
//
//					nbx  = normalArray[ i + 3 ];
//					nby  = normalArray[ i + 4 ];
//					nbz  = normalArray[ i + 5 ];
//
//					ncx  = normalArray[ i + 6 ];
//					ncy  = normalArray[ i + 7 ];
//					ncz  = normalArray[ i + 8 ];
//
//					nx = ( nax + nbx + ncx ) / 3;
//					ny = ( nay + nby + ncy ) / 3;
//					nz = ( naz + nbz + ncz ) / 3;
//
//					normalArray[ i ]   = nx;
//					normalArray[ i + 1 ] = ny;
//					normalArray[ i + 2 ] = nz;
//
//					normalArray[ i + 3 ] = nx;
//					normalArray[ i + 4 ] = ny;
//					normalArray[ i + 5 ] = nz;
//
//					normalArray[ i + 6 ] = nx;
//					normalArray[ i + 7 ] = ny;
//					normalArray[ i + 8 ] = nz;
//
//				}
//
//			}
//
//			getGL().bufferData( getGL().ARRAY_BUFFER, object.normalArray, getGL().DYNAMIC_DRAW );
//			enableAttribute( program.attributes.normal );
//			getGL().vertexAttribPointer( program.attributes.normal, 3, getGL().FLOAT, false, 0, 0 );
//
//		}
//
//		if ( object.hasUvs && material.map ) {
//
//			getGL().bindBuffer( getGL().ARRAY_BUFFER, object.__webglUvBuffer );
//			getGL().bufferData( getGL().ARRAY_BUFFER, object.uvArray, getGL().DYNAMIC_DRAW );
//			enableAttribute( program.attributes.uv );
//			getGL().vertexAttribPointer( program.attributes.uv, 2, getGL().FLOAT, false, 0, 0 );
//
//		}
//
//		if ( object.hasColors && material.vertexColors !== THREE.NoColors ) {
//
//			getGL().bindBuffer( getGL().ARRAY_BUFFER, object.__webglColorBuffer );
//			getGL().bufferData( getGL().ARRAY_BUFFER, object.colorArray, getGL().DYNAMIC_DRAW );
//			enableAttribute( program.attributes.color );
//			getGL().vertexAttribPointer( program.attributes.color, 3, getGL().FLOAT, false, 0, 0 );
//
//		}
//
//		disableUnusedAttributes();
//
//		getGL().drawArrays( getGL().TRIANGLES, 0, object.count );
//
//		object.count = 0;
//
//	}
	
	private void setupVertexAttributes( Material material, Shader program, BufferGeometry geometry, int startIndex ) {

		var geometryAttributes = geometry.attributes;

		var programAttributes = program.attributes;
		var programAttributesKeys = program.attributesKeys;

		for ( var i = 0, l = programAttributesKeys.length; i < l; i ++ ) {

			var key = programAttributesKeys[ i ];
			var programAttribute = programAttributes[ key ];

			if ( programAttribute >= 0 ) {

				var geometryAttribute = geometryAttributes[ key ];

				if ( geometryAttribute != undefined ) {

					var size = geometryAttribute.itemSize;

					gl.bindBuffer( _gl.ARRAY_BUFFER, geometryAttribute.buffer );

					enableAttribute( programAttribute );

					gl.vertexAttribPointer( programAttribute, size, _gl.FLOAT, false, 0, startIndex * size * 4 ); // 4 bytes per Float32

				} else if ( material.defaultAttributeValues !== undefined ) {

					if ( material.defaultAttributeValues[ key ].length === 2 ) {

						gl.vertexAttrib2fv( programAttribute, material.defaultAttributeValues[ key ] );

					} else if ( material.defaultAttributeValues[ key ].length === 3 ) {

						gl.vertexAttrib3fv( programAttribute, material.defaultAttributeValues[ key ] );

					}

				}

			}

		}

		disableUnusedAttributes();

	}

	
	//camera, lights, fog, material, geometry, object
	public void renderBufferDirect( Camera camera, List<Light> lights, AbstractFog fog, Material material, BufferGeometry geometry, GeometryObject object ) 
	{
		if ( ! material.isVisible() ) 
			return;

		Shader program = setProgram( camera, lights, fog, material, object );

		Map<String, Integer> attributes = material.getShader().getAttributesLocations();
		
		boolean updateBuffers = false;
		int wireframeBit = material instanceof HasWireframe && ((HasWireframe)material).isWireframe() ? 1 : 0;

		int geometryGroupHash = ( geometry.getId() * 0xffffff ) + ( material.getShader().getId() * 2 ) + wireframeBit;

		if ( geometryGroupHash != this._currentGeometryGroupHash ) 
		{
			this._currentGeometryGroupHash = geometryGroupHash;
			updateBuffers = true;
		}
		
		if ( updateBuffers ) {

			initAttributes();

		}

		WebGLRenderingContext gl = getGL();
				
		// render mesh

		if ( object instanceof Mesh ) 
		{
			BeginMode mode = material instanceof HasWireframe && ((HasWireframe)material).isWireframe() ? BeginMode.LINES : BeginMode.TRIANGLES;
			
			BufferAttribute index = geometry.getAttribute("index");
			
			if(index != null)
			{
				DrawElementsType type = DrawElementsType.UNSIGNED_SHORT;
				int size = 2;
				
				List<BufferGeometry.DrawCall> offsets = geometry.offsets;
				
				if ( offsets.size() == 0 ) {

					if ( updateBuffers ) {

						setupVertexAttributes( material, program, geometry, 0 );
						getGL().bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, index.buffer );

					}

					getGL().drawElements( mode, index.getArray().getLength(), type, 0 );

					this.info.getRender().calls ++;
					this.info.getRender().vertices += index.getArray().getLength(); // not really true, here vertices can be shared
					this.info.getRender().faces += index.getArray().getLength() / 3;

				} else {
					// if there is more than 1 chunk
					// must set attribute pointers to use new offsets for each chunk
					// even if geometry and materials didn't change

					updateBuffers = true;

					for ( int i = 0, il = offsets.size(); i < il; ++ i ) 
					{

						int startIndex = offsets.get( i ).index;

						if ( updateBuffers ) {

							setupVertexAttributes( material, program, geometry, startIndex );
							getGL().bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, index.buffer );

						}

						gl.drawElements( mode,  offsets.get( i ).count, type, offsets.get( i ).start );

						getInfo().getRender().calls ++;
						getInfo().getRender().vertices += offsets.get( i ).count; // not really true, here vertices can be shared
						getInfo().getRender().faces += offsets.get( i ).count / 3;
					}

				}
				
			} 
			else 
			{

				// non-indexed triangles

				if ( updateBuffers ) {

					setupVertexAttributes( material, program, geometry, 0 );

				}

				BufferAttribute position = geometry.getAttribute("position");

				// render non-indexed triangles

				gl.drawArrays( mode, 0, position.getArray().getLength() / 3 );

				this.info.getRender().calls ++;
				this.info.getRender().vertices += position.getArray().getLength() / 3;
				this.info.getRender().faces += position.getArray().getLength() / 9;

			}
			
		} 
		else if ( object instanceof PointCloud ) 
		{
			// render particles

			if ( updateBuffers ) {

				setupVertexAttributes( material, program, geometry, 0 );

			}

			BufferAttribute position = geometry.getAttribute("position");

			// render particles

			gl.drawArrays( BeginMode.POINTS, 0, position.getArray().getLength() / 3 );

			this.info.getRender().calls ++;
			this.info.getRender().points += position.getArray().getLength() / 3;
		} 
		else if ( object instanceof Line ) 
		{

			BeginMode mode = ( ((Line)object).getMode() == Line.MODE.STRIPS ) ? BeginMode.LINE_STRIP : BeginMode.LINES;

			setLineWidth( ((LineBasicMaterial)material.getLinewidth() );

			BufferAttribute index = geometry.getAttribute("index");

			if ( index != null ) {

				// indexed lines

//				var type, size;

//				if ( index.array instanceof Uint32Array ) {
//
//					type = _gl.UNSIGNED_INT;
//					size = 4;
//
//				} else {

					DrawElementsType type = DrawElementsType.UNSIGNED_SHORT;
					int size = 2;

//				}

				List<DrawCall> offsets = geometry.getOffsets();

				if ( offsets.size() == 0 ) {

					if ( updateBuffers ) {

						setupVertexAttributes( material, program, geometry, 0 );
						gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, index.buffer );

					}

					gl.drawElements( mode, index.getArray().getLength(), type, 0 ); // 2 bytes per Uint16Array

					this.info.getRender().calls ++;
					this.info.getRender().vertices += index.getArray().getLength(); // not really true, here vertices can be shared

				} else {

					// if there is more than 1 chunk
					// must set attribute pointers to use new offsets for each chunk
					// even if geometry and materials didn't change

					if ( offsets.size() > 1 ) updateBuffers = true;

					for ( int i = 0, il = offsets.size(); i < il; i ++ ) {

						int startIndex = offsets.get( i ).index;

						if ( updateBuffers ) {

							setupVertexAttributes( material, program, geometry, startIndex );
							gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, index.buffer );

						}

						// render indexed lines

						gl.drawElements( mode, offsets.get( i ).count, type, offsets.get( i ).start * size ); // 2 bytes per Uint16Array

						this.info.getRender().calls ++;
						this.info.getRender().vertices += offsets.get( i ).count; // not really true, here vertices can be shared

					}

				}

			} else {

				// non-indexed lines

				if ( updateBuffers ) {

					setupVertexAttributes( material, program, geometry, 0 );

				}

				BufferAttribute position = geometry.getAttribute("position");

				gl.drawArrays( mode, 0, position.getArray().getLength() / 3 );

				this.info.getRender().calls ++;
				this.info.getRender().points += position.getArray().getLength() / 3;

			}

		}
	}


	public void render( Scene scene, Camera camera )
	{
		render(scene, camera, null);
	}

	public void render( Scene scene, Camera camera, RenderTargetTexture renderTarget )
	{
		render(scene, camera, renderTarget, false);
	}
	
	private void projectObject( Object3D scene, Object3D object ) {

		if ( object.isVisible() == false ) return;

		if ( object instanceof Scene /*|| object instanceof Group */) {

			// skip

		} else {

			initObject( object, scene );

			if ( object instanceof Light ) {

				lights.add( (Light) object );

			} /*else if ( object instanceof Sprite ) {

				sprites.push( object );

			} else if ( object instanceof LensFlare ) {

				lensFlares.push( object );

			} */else {

				RendererObject webglObjects = this._webglObjects.get( object.getId() );

				if ( webglObjects != null && ( object.isFrustumCulled() == false || _frustum.isIntersectsObject( (GeometryObject) object ) == true ) ) {

					updateObject( object, scene );

					for ( int i = 0, l = webglObjects.size(); i < l; i ++ ) {

						RendererObject webglObject = webglObjects.get(i);

						unrollBufferMaterial( webglObject );

						webglObject.render = true;

//						if ( _this.sortObjects == true ) {
//
//							if ( object.renderDepth != null ) {
//
//								webglObject.z = object.renderDepth;
//
//							} else {
//
//								_vector3.setFromMatrixPosition( object.matrixWorld );
//								_vector3.applyProjection( _projScreenMatrix );
//
//								webglObject.z = _vector3.z;
//
//							}
//
//						}

					}

				}

			}

		}

		for ( int i = 0, l = object.getChildren().size(); i < l; i ++ ) {

			projectObject( scene, object.getChildren().get( i ) );

		}

	}
	

	/**
	 * Rendering.
	 * 
	 * @param scene        the {@link Scene} object.
	 * @param renderTarget optional
	 * @param forceClear   optional
	 */
	public void render( Scene scene, Camera camera, RenderTargetTexture renderTarget, boolean forceClear ) 
	{
		Log.debug("Called render()");
		
		AbstractFog fog = scene.getFog();

		// reset caching for this frame
		this._currentGeometryGroupHash = - 1;
		this._currentCamera = null;		
		this._currentMaterialId = -1;
		this._lightsNeedUpdate = true;
		
		if ( this.isAutoUpdateScene() ) 
		{
			scene.updateMatrixWorld(false);
		}

		// update camera matrices and frustum
		if ( camera.getParent() == null ) 
		{
			camera.updateMatrixWorld(false);
		}

		camera.getMatrixWorldInverse().getInverse( camera.getMatrixWorld() );
		
		_projScreenMatrix.multiply( camera.getProjectionMatrix(), camera.getMatrixWorldInverse() );
		_frustum.setFromMatrix( _projScreenMatrix );
		
		this.lights = new ArrayList<Light>();
		this.opaqueObjects = new ArrayList<RendererObject>();
		this.transparentObjects = new ArrayList<RendererObject>();
//		this.sprites.length = 0;
//		this.lensFlares.length = 0;

		projectObject( scene, scene );

		// custom render plugins (pre pass)
		renderPlugins( this.renderPluginsPre, camera );

		this.getInfo().getRender().calls = 0;
		this.getInfo().getRender().vertices = 0;
		this.getInfo().getRender().faces = 0;
		this.getInfo().getRender().points = 0;

		setRenderTarget( renderTarget );

		if ( this.isAutoClear() || forceClear )
		{
			clear( this.isAutoClearColor(), this.isAutoClearDepth(), this.isAutoClearStencil() );
		}
		
		// set matrices for immediate objects

		for ( int i = 0, il = this._webglObjectsImmediate.size(); i < il; i ++ ) {

			RendererObject webglObject = this._webglObjectsImmediate.get( i );
			Object3D object = webglObject.object;

			if ( object.isVisible() ) {

				setupMatrices( object, camera );

				webglObject.unrollImmediateBufferMaterial();

			}

		}

//		if ( this.isSortObjects() )
//			Collections.sort(renderList);

		if ( scene.overrideMaterial != null ) 
		{
			Log.error("render(): override material");
			
			Material material = scene.overrideMaterial;
			
			this.setBlending( material.getBlending(), material.getBlendEquation(), material.getBlendSrc(), material.getBlendDst() );
			this.setDepthTest( material.isDepthTest() );
			this.setDepthWrite( material.isDepthWrite() );
			setPolygonOffset( material.isPolygonOffset(), material.getPolygonOffsetFactor(), material.getPolygonOffsetUnits() );

			renderObjects( opaqueObjects, camera, lights, fog, true, material );
			renderObjects( transparentObjects, camera, lights, fog, true, material );
			renderObjectsImmediate( _webglObjectsImmediate, "", camera, lights, fog, false, material );
		} 
		else 
		{
			Log.debug("render(): NON override material");
			
			Material material = null;
			
			// opaque pass (front-to-back order)
			setBlending( Material.BLENDING.NO);

			renderObjects( opaqueObjects, camera, lights, fog, false, material );
			renderObjectsImmediate( _webglObjectsImmediate, 'opaque', camera, lights, fog, false, material );

			// transparent pass (back-to-front order)

			renderObjects( transparentObjects, camera, lights, fog, true, material );
			renderObjectsImmediate( _webglObjectsImmediate, 'transparent', camera, lights, fog, true, material );
		}

		// custom render plugins (post pass)
		renderPlugins( this.renderPluginsPost, camera );

		// Generate mipmap if we're using any kind of mipmap filtering
		if ( renderTarget != null && renderTarget.isGenerateMipmaps() 
				&& renderTarget.getMinFilter() != TextureMinFilter.NEAREST 
				&& renderTarget.getMinFilter() != TextureMinFilter.LINEAR)
		{
			renderTarget.updateRenderTargetMipmap(getGL());
		}

		// Ensure depth buffer writing is enabled so it can be cleared on next render

		this.setDepthTest( true );
		this.setDepthWrite( true );

//		 getGL().finish();
	}

	private void renderPlugins( List<Plugin> plugins, Camera camera ) 
	{
		if ( plugins.size() == 0 ) return;

		for ( int i = 0, il = plugins.size(); i < il; i ++ ) 
		{
			Plugin plugin = plugins.get( i );

			if( ! plugin.isEnabled() || plugin.isRendering() )
				return;

			plugin.setRendering(true);
			Log.debug("Called renderPlugins(): " + plugin.getClass().getName());

			// reset state for plugin (to start from clean slate)
			this._currentProgram = null;
			this._currentCamera = null;

			this.cache_oldBlending = null;
			this.cache_oldDepthTest = null;
			this.cache_oldDepthWrite = null;
			this.cache_oldMaterialSided = null;

			this._currentGeometryGroupHash = -1;
			this._currentMaterialId = -1;

			this._lightsNeedUpdate = true;

			plugin.render( camera, _currentWidth, _currentHeight );

			// reset state after plugin (anything could have changed)

			this._currentProgram = null;
			this._currentCamera = null;

			this.cache_oldBlending = null;
			this.cache_oldDepthTest = null;
			this.cache_oldDepthWrite = null;
			this.cache_oldMaterialSided = null;

			this._currentGeometryGroupHash = -1;
			this._currentMaterialId = -1;

			this._lightsNeedUpdate = true;
			
			plugin.setRendering(false);
		}
	}

	private void renderObjects (List<RendererObject> renderList, Camera camera, List<Light> lights, AbstractFog fog, boolean useBlending ) 
	{
		renderObjects ( renderList, camera, lights, fog, useBlending, null);
	}

	//renderList, camera, lights, fog, useBlending, overrideMaterial
	private void renderObjects (List<RendererObject> renderList, Camera camera, List<Light> lights, AbstractFog fog, boolean useBlending, Material overrideMaterial ) 
	{
		Log.debug("Called renderObjects() render list contains = " + renderList.size());
		
		Material material = null;
		
		for ( int i = renderList.size() - 1; i != - 1; i -- ) {

			RendererObject webglObject = renderList.get( i );

			GeometryObject object = webglObject.object;
			AbstractGeometry buffer = webglObject.buffer;

			setupMatrices( object, camera );

			if ( overrideMaterial != null) {

				material = overrideMaterial;

			} else {

				material = webglObject.material; //TODO: material = (isMaterialTransparent) ? webglObject.transparent : webglObject.opaque;

				if ( material == null ) continue;

				if ( useBlending ) setBlending( material.getBlending(), material.getBlendEquation(), material.getBlendSrc(), material.getBlendDst() );

				setDepthTest( material.isDepthTest() );
				setDepthWrite( material.isDepthWrite() );
				setPolygonOffset( material.isPolygonOffset(), material.getPolygonOffsetFactor(), material.getPolygonOffsetUnits() );

			}

			setMaterialFaces( material );

			if ( buffer instanceof BufferGeometry ) {

				renderBufferDirect( camera, lights, fog, material, (BufferGeometry)buffer, object );

			} else {

				renderBuffer( camera, lights, fog, material, (Geometry)buffer, object );

			}

		}

	}

	
	/**
	 * Buffer rendering.
	 * Render GeometryObject with material.
	 */
	//camera, lights, fog, material, geometryGroup, object
	public void renderBuffer( Camera camera, List<Light> lights, AbstractFog fog, Material material, Geometry geometry, GeometryObject object ) 
	{
		if ( ! material.isVisible() ) 
			return;

		Shader program = setProgram( camera, lights, fog, material, object );

		Map<String, Integer> attributes = material.getShader().getAttributesLocations();

		boolean updateBuffers = false;
		int wireframeBit = material instanceof HasWireframe && ((HasWireframe)material).isWireframe() ? 1 : 0;

		int geometryGroupHash = ( geometry.getId() * 0xffffff ) + ( material.getShader().getId() * 2 ) + wireframeBit;

//		GWT.log("--- renderBuffer() geometryGroupHash=" + geometryGroupHash 
//				+ ", _currentGeometryGroupHash=" +  this._currentGeometryGroupHash
//				+ ", program.id=" + program.id
//				+ ", geometryGroup.id=" + geometryBuffer.getId()
//				+ ", __webglLineCount=" + geometryBuffer.__webglLineCount
//				+ ", object.id=" + object.getId()
//				+ ", wireframeBit=" + wireframeBit);

		if ( geometryGroupHash != this._currentGeometryGroupHash ) 
		{
			this._currentGeometryGroupHash = geometryGroupHash;
			updateBuffers = true;
		}

		// vertices
		if ( !(material instanceof HasSkinning && ((HasSkinning)material).isMorphTargets()) && attributes.get("position") >= 0 ) 
		{
			if ( updateBuffers ) 
			{
				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglVertexBuffer );
				enableAttribute( attributes.get("position") );
				getGL().vertexAttribPointer( attributes.get("position"), 3, DataType.FLOAT, false, 0, 0 );
			}

		} 
		else if ( object instanceof Mesh && ((Mesh)object).morphTargetBase != null  ) 
		{
				setupMorphTargets( material, geometry, (Mesh)object );
		}

		
		if ( updateBuffers ) 
		{
			// custom attributes

			// Use the per-geometryGroup custom attribute arrays which are setup in initMeshBuffers

			if ( geometry.__webglCustomAttributesList != null ) 
			{
				for ( int i = 0; i < geometry.__webglCustomAttributesList.size(); i ++ ) 
				{
					Attribute attribute = geometry.__webglCustomAttributesList.get( i );

					if( attributes.get( attribute.belongsToAttribute ) >= 0 ) 
					{
						getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, attribute.buffer );
						enableAttribute( attributes.get( attribute.belongsToAttribute ) );
						getGL().vertexAttribPointer( attributes.get( attribute.belongsToAttribute ), attribute.size, DataType.FLOAT, false, 0, 0 );
					}
				}
			}

			// colors
			if ( attributes.get("color") >= 0 ) 
			{
				if ( geometry.getColors().size() > 0 || geometry.getFaces().size() > 0 ) {

					getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglColorBuffer );
					enableAttribute( attributes.get("color") );
					getGL().vertexAttribPointer( attributes.get("color"), 3, DataType.FLOAT, false, 0, 0 );

				} else {

					double defaultAttributeValues[] = new double[] {1.0,1.0,1.0};
					
					getGL().vertexAttrib3fv( attributes.get("color"), defaultAttributeValues);

				}
			}

			// normals
			if ( attributes.get("normal") >= 0 )
			{
				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglNormalBuffer );
				enableAttribute( attributes.get("normal") );
				getGL().vertexAttribPointer( attributes.get("normal"), 3, DataType.FLOAT, false, 0, 0 );
			}

			// tangents
			if ( attributes.get("tangent") >= 0 ) 
			{
				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglTangentBuffer );
				enableAttribute( attributes.get("tangent") );
				getGL().vertexAttribPointer( attributes.get("tangent"), 4, DataType.FLOAT, false, 0, 0 );
			}

			// uvs
			if ( attributes.get("uv") >= 0 )
			{
				if ( ((Geometry)object.getGeometry()).getFaceVertexUvs().get( 0 ) != null ) 
				{
					getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglUVBuffer );
					enableAttribute( attributes.get("uv") );
					getGL().vertexAttribPointer( attributes.get("uv"), 2, DataType.FLOAT, false, 0, 0 );

				} else {
					
					double defaultAttributeValues[] = new double[] {0.0,0.0};
					getGL().vertexAttrib2fv( attributes.get("uv"), defaultAttributeValues );
				}
			}

			if ( attributes.get("uv2") >= 0 ) 
			{
				if ( ((Geometry)object.getGeometry()).getFaceVertexUvs().get( 1 ) != null ) 
				{
					getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglUV2Buffer );
					enableAttribute( attributes.get("uv2") );
					getGL().vertexAttribPointer( attributes.get("uv2"), 2, DataType.FLOAT, false, 0, 0 );

					getGL().enableVertexAttribArray( attributes.get("uv2") );

				} else {
					
					double defaultAttributeValues[] = new double[] {0.0,0.0};
					
					getGL().vertexAttrib2fv( attributes.get("uv2"), defaultAttributeValues );
				}
			}

			if ( material instanceof HasSkinning && ((HasSkinning)material).isSkinning() &&
				 attributes.get("skinIndex") >= 0 && attributes.get("skinWeight") >= 0 ) 
			{
				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglSkinIndicesBuffer );
				enableAttribute( attributes.get("skinIndex") );
				getGL().vertexAttribPointer( attributes.get("skinIndex"), 4, DataType.FLOAT, false, 0, 0 );

				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglSkinWeightsBuffer );
				enableAttribute( attributes.get("skinWeight") );
				getGL().vertexAttribPointer( attributes.get("skinWeight"), 4, DataType.FLOAT, false, 0, 0 );
			}
			
			// line distances

			if ( attributes.get("lineDistance") >= 0 ) {

				getGL().bindBuffer( BufferTarget.ARRAY_BUFFER, geometry.__webglLineDistanceBuffer );
				enableAttribute( attributes.get("lineDistance") );
				getGL().vertexAttribPointer( attributes.get("lineDistance"), 1, DataType.FLOAT, false, 0, 0 );

			}

		}
		
		disableUnusedAttributes();

		Log.debug(" -> renderBuffer() ID " + object.getId() + " = " + object.getClass().getName());

		// Render object's buffers
		object.renderBuffer(this, geometry, updateBuffers);
	}

	private void initMaterial ( Material material, List<Light> lights, AbstractFog fog, GeometryObject object ) 
	{
		Log.debug("Called initMaterial for material: " + material.getClass().getName() + " and object " + object.getClass().getName());

		// heuristics to create shader parameters according to lights in the scene
		// (not to blow over maxLights budget)
		Map<String, Integer> maxLightCount = allocateLights( lights );
		int maxShadows = allocateShadows( lights );

		ProgramParameters parameters = new ProgramParameters();
		
		parameters.gammaInput  = isGammaInput();
		parameters.gammaOutput = isGammaOutput();
		parameters.physicallyBasedShading = isPhysicallyBasedShading();
		parameters.isSupportsVertexTextures = this.isGPUsupportsVertexTextures;
		
		parameters.useFog  = (fog != null);
		parameters.useFog2 = (fog != null && fog.getClass() == FogExp2.class);

		parameters.maxBones = allocateBones( object );

//		if(object instanceof SkinnedMesh)
//		{
//			parameters.useVertexTexture = this.isGPUsupportsBoneTextures && ((SkinnedMesh)object).useVertexTexture;
//			parameters.boneTextureWidth = ((SkinnedMesh)object).boneTextureWidth;
//			parameters.boneTextureHeight = ((SkinnedMesh)object).boneTextureHeight;
//		}

		parameters.maxMorphTargets = this.maxMorphTargets;
		parameters.maxMorphNormals = this.maxMorphNormals;

		parameters.maxDirLights   = maxLightCount.get("directional");
		parameters.maxPointLights = maxLightCount.get("point");
		parameters.maxSpotLights  = maxLightCount.get("spot");
		parameters.maxHemiLights  = maxLightCount.get("hemi");
		
		parameters.maxShadows = maxShadows;
		
		for(Plugin plugin: this.renderPluginsPre)
		if(plugin instanceof ShadowMap && ((ShadowMap)plugin).isEnabled() && object.isReceiveShadow())
		{
			parameters.shadowMapEnabled = true;
			parameters.shadowMapSoft    = ((ShadowMap)plugin).isSoft();
			parameters.shadowMapDebug   = ((ShadowMap)plugin).isDebugEnabled();
			parameters.shadowMapCascade = ((ShadowMap)plugin).isCascade();
		}

		material.updateProgramParameters(parameters);
		Log.debug("initMaterial() called new Program");

		String cashKey = material.getShader().getFragmentSource() 
				+ material.getShader().getVertexSource()
				+ parameters.toString();

		if(this._programs.containsKey(cashKey))
		{
			material.setShader( this._programs.get(cashKey) );
		}
		else
		{
			Shader shader = material.buildShader(getGL(), parameters);

			this._programs.put(cashKey, shader);

			this.getInfo().getMemory().programs = _programs.size();
		}
		
		Map<String, Integer> attributes = material.getShader().getAttributesLocations();

		if ( attributes.get("position") >= 0 ) 
			getGL().enableVertexAttribArray( attributes.get("position") );

		if ( attributes.get("color") >= 0 ) 
			getGL().enableVertexAttribArray( attributes.get("color") );

		if ( attributes.get("normal") >= 0 ) 
			getGL().enableVertexAttribArray( attributes.get("normal") );

		if ( attributes.get("tangent") >= 0 ) 
			getGL().enableVertexAttribArray( attributes.get("tangent") );

		if ( material instanceof HasSkinning && ((HasSkinning)material).isSkinning() &&
			 attributes.get("skinIndex") >= 0 && attributes.get("skinWeight") >= 0 
		) {
			getGL().enableVertexAttribArray( attributes.get("skinIndex") );
			getGL().enableVertexAttribArray( attributes.get("skinWeight") );
		}

		if ( attributes != null )
			for ( Integer a : attributes.values() )
				if( a != null && a >= 0 ) 
					getGL().enableVertexAttribArray( a );

		if(material instanceof ShaderMaterial)
		{
			if ( ((HasSkinning)material).isMorphTargets()) 
			{
				int numSupportedMorphTargets = 0;
				for ( int i = 0; i < this.maxMorphTargets; i ++ ) 
				{
					String id = "morphTarget" + i;

					if ( attributes.get( id ) >= 0 ) 
					{
						getGL().enableVertexAttribArray( attributes.get( id ) );
						numSupportedMorphTargets ++;
					}
				}
				
				((ShaderMaterial)material).setNumSupportedMorphTargets(numSupportedMorphTargets);
			}

			if ( ((HasSkinning)material).isMorphNormals() ) 
			{
				int numSupportedMorphNormals = 0;
				for ( int i = 0; i < this.maxMorphNormals; i ++ ) 
				{
					String id = "morphNormal" + i;

					if ( attributes.get( id ) >= 0 ) 
					{
						getGL().enableVertexAttribArray( attributes.get( id ) );
						numSupportedMorphNormals ++;
					}
				}

				((ShaderMaterial)material).setNumSupportedMorphNormals(numSupportedMorphNormals);
			}
		}
	}

	private Shader setProgram( Camera camera, List<Light> lights, AbstractFog fog, Material material, GeometryObject object ) 
	{
		// Use new material units for new shader
		this._usedTextureUnits = 0;
		
		if(material.isNeedsUpdate()) 
		{
			if(material.getShader() == null || material.getShader().getProgram() == null)
				material.deallocate(this);
			
			initMaterial( material, lights, fog, object );
			material.setNeedsUpdate(false);
		}

		if ( material instanceof HasSkinning && ((HasSkinning)material).isMorphTargets() ) 
		{
			if ( object instanceof Mesh && ((Mesh)object).__webglMorphTargetInfluences == null ) 
			{
				((Mesh)object).__webglMorphTargetInfluences = Float32Array.create( this.maxMorphTargets );
			}
		}

		boolean refreshProgram = false;
		boolean refreshMaterial = false;
		boolean refreshLights = false;

		Shader shader = material.getShader(); 
		WebGLProgram program = shader.getProgram();
		Map<String, Uniform> m_uniforms = shader.getUniforms();

		if ( program != _currentProgram )
		{
			getGL().useProgram( program );
			this._currentProgram = program;

			refreshProgram = true;
			refreshMaterial = true;
			refreshLights = true;

			Log.error("program != _currentProgram");
		}

		if ( material.getId() != this._currentMaterialId ) 
		{
			this._currentMaterialId = material.getId();
			refreshMaterial = true;
		}

		if ( refreshMaterial || camera != this._currentCamera ) 
		{
			getGL().uniformMatrix4fv( m_uniforms.get("projectionMatrix").getLocation(), false, camera.getProjectionMatrix().getArray() );

			if ( camera != this._currentCamera ) 
				this._currentCamera = camera;
		}

		// skinning uniforms must be set even if material didn't change
		// auto-setting of texture unit for bone texture must go before other textures
		// not sure why, but otherwise weird things happen
		if ( material instanceof HasSkinning && ((HasSkinning)material).isSkinning() )
		{
			if ( object instanceof SkinnedMesh && ((SkinnedMesh)object).useVertexTexture && this.isGPUsupportsBoneTextures) 
			{
				if ( m_uniforms.get("boneTexture").getLocation() != null ) 
				{
					int textureUnit = getTextureUnit();

					getGL().uniform1i( m_uniforms.get("boneTexture").getLocation(), textureUnit );
					setTexture( ((SkinnedMesh)object).boneTexture, textureUnit );
				}
			} 
			else 
			{
				if ( m_uniforms.get("boneGlobalMatrices").getLocation() != null ) 
				{
					getGL().uniformMatrix4fv( m_uniforms.get("boneGlobalMatrices").getLocation(), false, ((SkinnedMesh)object).boneMatrices );
				}
			}
		}
		
		if ( refreshMaterial ) 
		{
			// refresh uniforms common to several materials
			if ( fog != null && material instanceof HasFog && ((HasFog)material).isFog())
				fog.refreshUniforms( m_uniforms );

			if ( material.getClass() == MeshPhongMaterial.class ||
				 material.getClass() == MeshLambertMaterial.class ||
				 (material.getClass() == ShaderMaterial.class && ((ShaderMaterial)material).isLights())) 
			{

				if (this._lightsNeedUpdate ) 
				{
					this.cache_lights.setupLights( lights, this.isGammaInput );
					this._lightsNeedUpdate = false;
				}

				this.cache_lights.refreshUniformsLights( m_uniforms );
			}

			material.refreshUniforms(camera, this.isGammaInput);

			if ( object.isReceiveShadow() && ! material.isShadowPass() )
				refreshUniformsShadow( m_uniforms, lights );

			// load common uniforms
			loadUniformsGeneric( m_uniforms );

			// load material specific uniforms
			// (shader material also gets them for the sake of genericity)
			if ( material.getClass() == ShaderMaterial.class ||
				 material.getClass() == MeshPhongMaterial.class ||
				 material instanceof HasEnvMap 
			) {

				if ( m_uniforms.get("cameraPosition").getLocation() != null ) 
				{
					Vector3 position = camera.getMatrixWorld().getPosition();
					getGL().uniform3f( m_uniforms.get("cameraPosition").getLocation(), position.getX(), position.getY(), position.getZ() );
				}
			}

			if ( material.getClass() == MeshPhongMaterial.class ||
				 material.getClass() == MeshLambertMaterial.class ||
				 material.getClass() == ShaderMaterial.class ||
				 material instanceof HasSkinning && ((HasSkinning)material).isSkinning() 
			) {

				if ( m_uniforms.get("viewMatrix").getLocation() != null ) 
					getGL().uniformMatrix4fv( m_uniforms.get("viewMatrix").getLocation(), false, camera._viewMatrixArray );
			}
		}

		loadUniformsMatrices( m_uniforms, object );

		if ( m_uniforms.get("modelMatrix").getLocation() != null )
			getGL().uniformMatrix4fv( m_uniforms.get("modelMatrix").getLocation(), false, object.getMatrixWorld().getArray() );

		return shader;
	}

	private void refreshUniformsShadow( Map<String, Uniform> uniforms, List<Light> lights ) 
	{
		if ( uniforms.containsKey("shadowMatrix") ) 
		{
			// Make them zero
			uniforms.get("shadowMap").setValue(new ArrayList<Texture>());
			uniforms.get("shadowMapSize").setValue(new ArrayList<Vector2>());
			uniforms.get("shadowMatrix").setValue(new ArrayList<Matrix4>());
			List<Texture> shadowMap = (List<Texture>)uniforms.get("shadowMap").getValue();
			List<Vector2> shadowMapSize = (List<Vector2>)uniforms.get("shadowMapSize").getValue();
			List<Matrix4> shadowMatrix = (List<Matrix4>)uniforms.get("shadowMatrix").getValue();
			
			int j = 0;
			for ( Light light: lights) 
			{
				if ( ! light.isCastShadow() ) continue;

				if ( light instanceof ShadowLight && ! ((ShadowLight)light).isShadowCascade() )  
				{
					ShadowLight shadowLight = (ShadowLight) light;

					shadowMap.add(shadowLight.getShadowMap() );
					shadowMapSize.add(shadowLight.getShadowMapSize() );
					shadowMatrix.add(shadowLight.getShadowMatrix() );

					((Float32Array)uniforms.get("shadowDarkness").getValue()).set( j, shadowLight.getShadowDarkness() );
					((Float32Array)uniforms.get("shadowBias").getValue()).set( j, shadowLight.getShadowBias() );
					j++;
				}
			}
		}
	}

	// Uniforms (load to GPU)

	private void loadUniformsMatrices ( Map<String, Uniform> uniforms, GeometryObject object ) 
	{
		GeometryObject objectImpl = (GeometryObject) object;
		getGL().uniformMatrix4fv( uniforms.get("modelViewMatrix").getLocation(), false, objectImpl._modelViewMatrix.getArray() );

		if ( uniforms.containsKey("normalMatrix") )
			getGL().uniformMatrix3fv( uniforms.get("normalMatrix").getLocation(), false, objectImpl._normalMatrix.getArray() );
	}

	@SuppressWarnings("unchecked")
	private void loadUniformsGeneric( Map<String, Uniform> materialUniforms ) 
	{
		for ( Uniform uniform : materialUniforms.values() ) 
		{
//			for ( String key: materialUniforms.keySet() ) 
//			{
//				 Uniform uniform = materialUniforms.get(key);
			WebGLUniformLocation location = uniform.getLocation();
		
			if ( location == null ) continue;

			Object value = uniform.getValue();
			Uniform.TYPE type = uniform.getType();
			
			// Up textures also for undefined values
			if ( type != Uniform.TYPE.T && value == null ) continue;

			//Log.debug("loadUniformsGeneric() " + uniform);
			
			WebGLRenderingContext gl = getGL();

			if(type == TYPE.I) // single integer
			{
				gl.uniform1i( location, (value instanceof Boolean) ? ((Boolean)value) ? 1 : 0 : (Integer) value );
			}
			else if(type == TYPE.F) // single double
			{
				gl.uniform1f( location, (Double)value );
			}
			else if(type == TYPE.V2) // single Vector2
			{ 
				gl.uniform2f( location, ((Vector2)value).getX(), ((Vector2)value).getX() );
			}
			else if(type == TYPE.V3) // single Vector3
			{ 
				gl.uniform3f( location, ((Vector3)value).getX(), ((Vector3)value).getY(), ((Vector3)value).getZ() );
			}
			else if(type == TYPE.V4) // single Vector4
			{
				gl.uniform4f( location, ((Vector4)value).getX(), ((Vector4)value).getY(), ((Vector4)value).getZ(), ((Vector4)value).getW() );
			}
			else if(type == TYPE.C) // single Color
			{
				gl.uniform3f( location, ((Color)value).getR(), ((Color)value).getG(), ((Color)value).getB() );
			}
			else if(type == TYPE.FV1) // flat array of floats (JS or typed array)
			{
				gl.uniform1fv( location, (Float32Array)value );
			}
			else if(type == TYPE.FV) // flat array of floats with 3 x N size (JS or typed array)
			{ 
				gl.uniform3fv( location, (Float32Array) value );
			}
			else if(type == TYPE.V2V) // List of Vector2
			{ 
				List<Vector2> listVector2f = (List<Vector2>) value;
				if ( uniform.getCacheArray() == null )
					uniform.setCacheArray( Float32Array.create( 2 * listVector2f.size() ) );

				for ( int i = 0, il = listVector2f.size(); i < il; i ++ ) 
				{
					int offset = i * 2;

					uniform.getCacheArray().set(offset, listVector2f.get(i).getX());
					uniform.getCacheArray().set(offset + 1, listVector2f.get(i).getY());
				}

				gl.uniform2fv( location, uniform.getCacheArray() );
			}
			else if(type == TYPE.V3V) // List of Vector3
			{
				List<Vector3> listVector3f = (List<Vector3>) value;
				if ( uniform.getCacheArray() == null )
					uniform.setCacheArray( Float32Array.create( 3 * listVector3f.size() ) );

				for ( int i = 0, il = listVector3f.size(); i < il; i ++ ) 
				{
					int offset = i * 3;

					uniform.getCacheArray().set(offset, listVector3f.get( i ).getX());
					uniform.getCacheArray().set(offset + 1, listVector3f.get( i ).getY());
					uniform.getCacheArray().set(offset + 2 , listVector3f.get( i ).getZ());
				}

				gl.uniform3fv( location, uniform.getCacheArray() );
			}
			else if(type == TYPE.V4V) // List of Vector4
			{
				List<Vector4> listVector4f = (List<Vector4>) value;
				if ( uniform.getCacheArray() == null)
					uniform.setCacheArray( Float32Array.create( 4 * listVector4f.size() ) );


				for ( int i = 0, il = listVector4f.size(); i < il; i ++ ) 
				{
					int offset = i * 4;

					uniform.getCacheArray().set(offset, listVector4f.get( i ).getX());
					uniform.getCacheArray().set(offset + 1, listVector4f.get( i ).getY());
					uniform.getCacheArray().set(offset + 2, listVector4f.get( i ).getZ());
					uniform.getCacheArray().set(offset + 3, listVector4f.get( i ).getW());
				}

				gl.uniform4fv( location, uniform.getCacheArray() );
			}
			else if(type == TYPE.M4) // single Matrix4
			{
				Matrix4 matrix4 = (Matrix4) value;
				if ( uniform.getCacheArray() == null )
					uniform.setCacheArray( Float32Array.create( 16 ) );

				matrix4.flattenToArrayOffset( uniform.getCacheArray() );
				gl.uniformMatrix4fv( location, false, uniform.getCacheArray() );
			}
			else if(type == TYPE.M4V) // List of Matrix4
			{
				List<Matrix4> listMatrix4f = (List<Matrix4>) value;
				if ( uniform.getCacheArray() == null )
					uniform.setCacheArray( Float32Array.create( 16 * listMatrix4f.size() ) );

				for ( int i = 0, il = listMatrix4f.size(); i < il; i ++ )
					listMatrix4f.get( i ).flattenToArrayOffset( uniform.getCacheArray(), i * 16 );

				gl.uniformMatrix4fv( location, false, uniform.getCacheArray() );
			}
			else if(type == TYPE.T) // single Texture (2d or cube)
			{
				Texture texture = (Texture)value;
				int textureUnit = getTextureUnit();

				gl.uniform1i( location, textureUnit );

				if ( texture != null )
				{
					if ( texture.getClass() == CubeTexture.class )
						setCubeTexture( (CubeTexture) texture, textureUnit );

					else if ( texture.getClass() == RenderTargetCubeTexture.class )
						setCubeTextureDynamic( (RenderTargetCubeTexture)texture, textureUnit );

					else
						setTexture( texture, textureUnit );
				}
			}
			else if(type == TYPE.TV) //List of Texture (2d)
			{
				List<Texture> textureList = (List<Texture>)value;
				int[] units = new int[textureList.size()];

				for( int i = 0, il = textureList.size(); i < il; i ++ ) 
				{
					units[ i ] = getTextureUnit();
				}

				gl.uniform1iv( location, units );

				for( int i = 0, il = textureList.size(); i < il; i ++ ) 
				{
					Texture texture = textureList.get( i );
					int textureUnit = units[ i ];

					if ( texture == null ) continue;

					setTexture( texture, textureUnit );
				}
			}
		}
	}
	
	public int getTextureUnit() 
	{
		int textureUnit = this._usedTextureUnits ++;

		if ( textureUnit >= this.GPUmaxTextures ) 
		{
			Log.warn( "Trying to use " + textureUnit + " texture units while this GPU supports only " + this.GPUmaxTextures );
		}

		return textureUnit;
	}

	private void setupMatrices ( Object3D object, Camera camera ) 
	{
		object._modelViewMatrix.multiply( camera.getMatrixWorldInverse(), object.getMatrixWorld() );
		object._normalMatrix.getNormalMatrix( object._modelViewMatrix );
	}
	
	private void setMaterialFaces( Material material )
	{
		if ( this.cache_oldMaterialSided == null || this.cache_oldMaterialSided != material.getSides() ) 
		{
			if(material.getSides() == Material.SIDE.DOUBLE)
				getGL().disable( EnableCap.CULL_FACE );
			else
				getGL().enable( EnableCap.CULL_FACE );

			if ( material.getSides() == Material.SIDE.BACK ) 
				getGL().frontFace( FrontFaceDirection.CW );
			else
				getGL().frontFace( FrontFaceDirection.CCW );

			this.cache_oldMaterialSided = material.getSides();
		}
	}

	public void setDepthTest( boolean depthTest ) 
	{
		if ( this.cache_oldDepthTest == null || this.cache_oldDepthTest != depthTest ) 
		{
			if ( depthTest )
				getGL().enable( EnableCap.DEPTH_TEST );
			else 
				getGL().disable( EnableCap.DEPTH_TEST );

			this.cache_oldDepthTest = depthTest;
		}
	}

	public void setDepthWrite(boolean depthWrite ) 
	{
		if ( this.cache_oldDepthWrite == null || this.cache_oldDepthWrite != depthWrite ) 
		{
			getGL().depthMask( depthWrite );
			cache_oldDepthWrite = depthWrite;
		}
	}

	private void setPolygonOffset( boolean polygonoffset, double factor, double units ) 
	{
		if ( this.cache_oldPolygonOffset == null || this.cache_oldPolygonOffset != polygonoffset ) 
		{
			if ( polygonoffset )
				getGL().enable( EnableCap.POLYGON_OFFSET_FILL );
			else
				getGL().disable( EnableCap.POLYGON_OFFSET_FILL );

			this.cache_oldPolygonOffset = polygonoffset;
		}

		if ( polygonoffset && ( cache_oldPolygonOffsetFactor == null || 
				cache_oldPolygonOffsetUnits == null || 
				cache_oldPolygonOffsetFactor != factor || 
				cache_oldPolygonOffsetUnits != units ) 
		) {
			getGL().polygonOffset( factor, units );

			this.cache_oldPolygonOffsetFactor = factor;
			this.cache_oldPolygonOffsetUnits = units;
		}
	}

	public void setBlending( Material.BLENDING blending) 
	{
		if ( blending != this.cache_oldBlending ) 
		{
			if( blending == Material.BLENDING.NO) 
			{
				getGL().disable( EnableCap.BLEND );
				
			} 
			else if( blending == Material.BLENDING.ADDITIVE) 
			{
				getGL().enable( EnableCap.BLEND );
				getGL().blendEquation( BlendEquationMode.FUNC_ADD );
				getGL().blendFunc( BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE );
				
			// TODO: Find blendFuncSeparate() combination
			} 
			else if( blending == Material.BLENDING.SUBTRACTIVE) 
			{
				getGL().enable( EnableCap.BLEND );
				getGL().blendEquation( BlendEquationMode.FUNC_ADD );
				getGL().blendFunc( BlendingFactorSrc.ZERO, BlendingFactorDest.ONE_MINUS_SRC_COLOR );

			// TODO: Find blendFuncSeparate() combination
			} 
			else if( blending == Material.BLENDING.MULTIPLY) 
			{
				getGL().enable( EnableCap.BLEND );
				getGL().blendEquation( BlendEquationMode.FUNC_ADD );
				getGL().blendFunc( BlendingFactorSrc.ZERO, BlendingFactorDest.SRC_COLOR );

			} 
			else if( blending == Material.BLENDING.CUSTOM) 
			{
				getGL().enable( EnableCap.BLEND );

			} 
			else 
			{
				getGL().enable( EnableCap.BLEND );
				getGL().blendEquationSeparate( BlendEquationMode.FUNC_ADD, BlendEquationMode.FUNC_ADD );
				getGL().blendFuncSeparate( BlendingFactorSrc.SRC_ALPHA, 
						BlendingFactorDest.ONE_MINUS_SRC_ALPHA, 
						BlendingFactorSrc.ONE, 
						BlendingFactorDest.ONE_MINUS_SRC_ALPHA );
			}

			this.cache_oldBlending = blending;
		}
		
		this.cache_oldBlendEquation = null;
		this.cache_oldBlendSrc = null;
		this.cache_oldBlendDst = null;
	}

	private void setBlending( Material.BLENDING blending, BlendEquationMode blendEquation, BlendingFactorSrc blendSrc, BlendingFactorDest blendDst ) 
	{
		setBlending(blending);

		if ( blending == Material.BLENDING.CUSTOM ) 
		{
			if ( blendEquation != this.cache_oldBlendEquation ) 
			{
				getGL().blendEquation( blendEquation );
				this.cache_oldBlendEquation = blendEquation;
			}

			if ( blendSrc != cache_oldBlendSrc || blendDst != cache_oldBlendDst ) 
			{
				getGL().blendFunc( blendSrc, blendDst);

				this.cache_oldBlendSrc = blendSrc;
				this.cache_oldBlendDst = blendDst;
			}
		}
	}

	// Textures
	
	private void setCubeTextureDynamic(RenderTargetCubeTexture texture, int slot) 
	{
		getGL().activeTexture( TextureUnit.TEXTURE0, slot );
		getGL().bindTexture( TextureTarget.TEXTURE_CUBE_MAP, texture.getWebGlTexture() );
	}

	public void setTexture( Texture texture, int slot ) 
	{
		if ( texture.isNeedsUpdate()) 
		{
			if ( texture.getWebGlTexture() == null ) 
			{
				texture.setWebGlTexture( getGL().createTexture() );

				this.getInfo().getMemory().textures ++;
			}
			
			getGL().activeTexture( TextureUnit.TEXTURE0, slot );
			getGL().bindTexture( TextureTarget.TEXTURE_2D, texture.getWebGlTexture() );

			getGL().pixelStorei( PixelStoreParameter.UNPACK_FLIP_Y_WEBGL, texture.isFlipY() ? 1 : 0 );
			getGL().pixelStorei( PixelStoreParameter.UNPACK_PREMULTIPLY_ALPHA_WEBGL, texture.isPremultiplyAlpha() ? 1 : 0 );

			Element image = texture.getImage();
			boolean isImagePowerOfTwo = Mathematics.isPowerOfTwo( image.getOffsetWidth() ) 
					&& Mathematics.isPowerOfTwo( image.getOffsetHeight() );

			texture.setTextureParameters( getGL(), this.GPUmaxAnisotropy, TextureTarget.TEXTURE_2D, isImagePowerOfTwo );

			if ( texture instanceof CompressedTexture ) 
			{
				List<DataTexture> mipmaps = ((CompressedTexture) texture).getMipmaps();

				for( int i = 0, il = mipmaps.size(); i < il; i ++ ) 
				{
					DataTexture mipmap = mipmaps.get( i );
					getGL().compressedTexImage2D( TextureTarget.TEXTURE_2D, i, ((CompressedTexture) texture).getCompressedFormat(), 
							mipmap.getWidth(), mipmap.getHeight(), 0, mipmap.getData() );
				}
			}
			else if ( texture instanceof DataTexture ) 
			{
				getGL().texImage2D( TextureTarget.TEXTURE_2D, 0, 
						((DataTexture) texture).getWidth(),
						((DataTexture) texture).getHeight(), 
						0, 
						texture.getFormat(), 
						texture.getType(),
						((DataTexture) texture).getData() );
			} 
			else 
			{
				getGL().texImage2D( TextureTarget.TEXTURE_2D, 0, texture.getFormat(), texture.getType(), (ImageElement)image );
			}

			if ( texture.isGenerateMipmaps() && isImagePowerOfTwo ) 
				getGL().generateMipmap( TextureTarget.TEXTURE_2D );

			texture.setNeedsUpdate(false);
		} 
		// Needed to check webgl texture in case deferred loading
		else if(texture.getWebGlTexture() != null)
		{
			getGL().activeTexture( TextureUnit.TEXTURE0, slot );
			getGL().bindTexture( TextureTarget.TEXTURE_2D, texture.getWebGlTexture() );
		}
	}
	
	private CanvasElement createPowerOfTwoImage(Element image) 
	{
		int width = image.getOffsetWidth();
		int height = image.getOffsetHeight();
		
		CanvasElement canvas = Document.get().createElement("canvas").cast();
		
		// Scale up the texture to the next highest power of two dimensions.
		canvas.setWidth( Mathematics.getNextHighestPowerOfTwo( width ) );
		canvas.setHeight( Mathematics.getNextHighestPowerOfTwo( height ) );

		Context2d context = canvas.getContext2d();
		context.drawImage((ImageElement)image, 0, 0, width, height);
		
		return canvas;
	}

	/**
	 * Warning: Scaling through the canvas will only work with images that use
	 * premultiplied alpha.
	 * 
	 * @param image    the image element
	 * @param maxSize  the max size of absoluteWidth or absoluteHeight
	 * 
	 * @return the image element (Canvas or Image)
	 */
	private Element clampToMaxSize ( Element image, int maxSize ) 
	{
		int imgWidth = image.getOffsetWidth();
		int imgHeight = image.getOffsetHeight();

		if ( imgWidth <= maxSize && imgHeight <= maxSize )
			return image;

		int maxDimension = Math.max( imgWidth, imgHeight );
		int newWidth = (int) Math.floor( imgWidth * maxSize / maxDimension );
		int newHeight = (int) Math.floor( imgHeight * maxSize / maxDimension );

		CanvasElement canvas = Document.get().createElement("canvas").cast();
		canvas.setWidth(newWidth);
		canvas.setHeight(newHeight);
		
		Context2d context = canvas.getContext2d();
		context.drawImage((ImageElement)image, 0, 0, imgWidth, imgHeight, 0, 0, newWidth, newHeight );

		return canvas;
	}

	private void setCubeTexture ( CubeTexture texture, int slot ) 
	{
		if ( !texture.isValid() )
			return;

		if ( texture.isNeedsUpdate() ) 
		{
			if ( texture.getWebGlTexture() == null )
			{
				texture.setWebGlTexture(getGL().createTexture());
				this.getInfo().getMemory().textures += 6;
			}

			getGL().activeTexture( TextureUnit.TEXTURE0, slot );
			getGL().bindTexture( TextureTarget.TEXTURE_CUBE_MAP, texture.getWebGlTexture() );
			getGL().pixelStorei( PixelStoreParameter.UNPACK_FLIP_Y_WEBGL, texture.isFlipY() ? 1 : 0 );

			List<Element> cubeImage = new ArrayList<Element>();

			for ( int i = 0; i < 6; i ++ ) 
			{
				if ( this.isAutoScaleCubemaps )
					cubeImage.add(clampToMaxSize( texture.getImage( i ), this.GPUmaxCubemapSize ));

				else
					cubeImage.add(texture.getImage( i ));
			}

			Element image = cubeImage.get( 0 );
			boolean isImagePowerOfTwo = Mathematics.isPowerOfTwo( image.getOffsetWidth() ) 
					&& Mathematics.isPowerOfTwo( image.getOffsetHeight() );

			texture.setTextureParameters( getGL(), this.GPUmaxAnisotropy, TextureTarget.TEXTURE_CUBE_MAP, true /*power of two*/ );

			for ( int i = 0; i < 6; i ++ ) 
			{
				if(!isImagePowerOfTwo)
				{
					getGL().texImage2D( TextureTarget.TEXTURE_CUBE_MAP_POSITIVE_X, i, 0, 
							texture.getFormat(), texture.getType(), createPowerOfTwoImage( cubeImage.get( i ) ) );
				}
				else
				{
					getGL().texImage2D( TextureTarget.TEXTURE_CUBE_MAP_POSITIVE_X, i, 0, 
							texture.getFormat(), texture.getType(), (ImageElement)cubeImage.get( i ) );
				}
			}

			if ( texture.isGenerateMipmaps() )	
				getGL().generateMipmap( TextureTarget.TEXTURE_CUBE_MAP );

			texture.setNeedsUpdate(false);
		} 
		else 
		{
			getGL().activeTexture( TextureUnit.TEXTURE0, slot );
			getGL().bindTexture( TextureTarget.TEXTURE_CUBE_MAP, texture.getWebGlTexture() );
		}

	}

	/**
	 * Setup render target
	 * 
	 * @param renderTarget the render target
	 */
	public void setRenderTarget( RenderTargetTexture renderTarget ) 
	{
		Log.debug("Called setRenderTarget(params)");
		WebGLFramebuffer framebuffer = null;
		
		if(renderTarget != null) 
		{
			renderTarget.setRenderTarget(getGL());
		    framebuffer = renderTarget.getWebGLFramebuffer();

			this._currentWidth = renderTarget.getWidth();
			this._currentHeight = renderTarget.getHeight();

		} 
		else 
		{
			this._currentWidth = this.viewportWidth;
			this._currentHeight = this.viewportHeight;
		}

		if ( framebuffer != this._currentFramebuffer ) 
		{
			getGL().bindFramebuffer( framebuffer );
			getGL().viewport( 0, 0, this._currentWidth, this._currentHeight );

			this._currentFramebuffer = framebuffer;
		}
	}

	/**
	 * Default for when object is not specified
	 * ( for example when prebuilding shader to be used with multiple objects )
	 * 
	 * - leave some extra space for other uniforms
	 * - limit here is ANGLE's 254 max uniform vectors (up to 54 should be safe)
	 * 
	 * @param object
	 * @return
	 */
	private int allocateBones (GeometryObject object ) 
	{
		if ( this.isGPUsupportsBoneTextures && object instanceof SkinnedMesh && ((SkinnedMesh)object).useVertexTexture ) 
		{
			return 1024;
		} 
		else 
		{
			// default for when object is not specified
			// ( for example when prebuilding shader
			//   to be used with multiple objects )
			//
			// 	- leave some extra space for other uniforms
			//  - limit here is ANGLE's 254 max uniform vectors
			//    (up to 54 should be safe)

			int nVertexUniforms = getGL().getParameteri( WebGLConstants.MAX_VERTEX_UNIFORM_VECTORS );
			int nVertexMatrices = (int) Math.floor( ( nVertexUniforms - 20 ) / 4 );

			int maxBones = nVertexMatrices;

			if ( object instanceof SkinnedMesh ) 
			{
				maxBones = Math.min( ((SkinnedMesh)object).bones.size(), maxBones );

				if ( maxBones < ((SkinnedMesh)object).bones.size() )
				{
					Log.warn( "WebGLRenderer: too many bones - " + ((SkinnedMesh)object).bones.size() 
							+ ", this GPU supports just " + maxBones + " (try OpenGL instead of ANGLE)" );
				}
			}

			return maxBones;
		}
	}

	private Map<String, Integer> allocateLights ( List<Light> lights ) 
	{
		int dirLights = 0, pointLights = 0, spotLights = 0, hemiLights = 0;
		
		int maxDirLights = 0, maxPointLights = 0, maxSpotLights = 0, maxHemiLights = 0;
		
		for(Light light: lights) 
		{
			if ( light instanceof ShadowLight && ((ShadowLight)light).isOnlyShadow() ) continue;

			if ( light instanceof DirectionalLight ) dirLights ++;
			if ( light instanceof PointLight ) pointLights ++;
			if ( light instanceof SpotLight ) spotLights ++;
			if ( light instanceof HemisphereLight ) hemiLights ++;
		}

		Map<String, Integer> retval = GWT.isScript() ? 
				new FastMap<Integer>() : new HashMap<String, Integer>();
		retval.put("directional", maxDirLights);
		retval.put("point", maxPointLights);
		retval.put("spot", maxSpotLights);
		retval.put("hemi", maxHemiLights);

		return retval;
	}

	private int allocateShadows( List<Light> lights ) 
	{
		int maxShadows = 0;

		for (Light light: lights)
		{
			if ( light instanceof ShadowLight)
			{
				if( !((ShadowLight)light).isCastShadow() )
					continue;

				maxShadows ++;
			}
		}

		return maxShadows;
	}
}
