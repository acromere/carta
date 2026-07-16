package com.acromere.cartesia.tool.design;

import com.acromere.annotation.Note;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.*;
import com.acromere.cartesia.tool.Workplane;
import com.acromere.cartesia.tool.design.binding.DesignBinding;
import com.acromere.cartesia.tool.design.binding.DesignDoubleBinding;
import com.acromere.cartesia.tool.design.binding.PathElementMapper;
import com.acromere.data.NodeEvent;
import com.acromere.event.EventHandler;
import com.acromere.zerra.javafx.Fx;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.*;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class DesignToolV3Renderer extends BaseDesignRenderer {

	private static final String FX_GEOMETRY = "fx-geometry";

	private static final PathElementMapper pathElementMapper;

	/**
	 * Caution: This map is shared among all renderers of this type. This could
	 * result in a memory leak if the FX geometry is not properly removed from the
	 * renderers when drawables are removed from the design, tools are closed,
	 * etc. Watch for memory leaks.
	 */
	private static final Map<GeometryKey, Node> drawableToGeometry;

	/**
	 * Reference to the Design.
	 */
	private Design<? extends DesignModel> design;

	/**
	 * Reference to the Design data model.
	 */
	private DesignModel model;

	/**
	 * Reference to the DesignTool workplane.
	 */
	private Workplane workplane;

	/**
	 * The primary container for all visual elements that are not part of the design
	 * in the renderer. Examples include the orientation indicator.
	 * <p>
	 * This field is immutable and is used internally to manage the rendering system's
	 * screen-level components.
	 */
	@Getter
	final Pane screen;

	/**
	 * Represents the primary rendering pane for the design in the renderer.
	 * This pane serves as the container for all graphical components and sublayers
	 * that are part of the design. It acts as the central element around which
	 * other panes or layers may be structured to compose the complete design visualization.
	 * <p>
	 * This field is immutable and is used internally to manage the rendering system's
	 * design-level components.
	 */
	@Getter
	final Pane world;

	/**
	 * The geometry in this pane is configured by the workplane but managed
	 * internally so that it can be optimized the use of the FX geometry.
	 */
	@Getter
	final Pane grid;

	/**
	 * This pane contains all the design layers.
	 */
	@Getter
	final Pane layers;

	/**
	 * The reference geometry layer.
	 */
	@Getter
	final Pane reference;

	/**
	 * The preview geometry layer.
	 */
	@Getter
	final Pane preview;

	private final DoubleProperty shapeScaleX;

	private final DoubleProperty shapeScaleY;

	private final DoubleProperty unitScale;

	@Getter( AccessLevel.PACKAGE )
	private final DoubleProperty rendererCenterX;

	@Getter( AccessLevel.PACKAGE )
	private final DoubleProperty rendererCenterY;

	@Getter( AccessLevel.PACKAGE )
	private final Scale viewZoomTransform;

	@Getter( AccessLevel.PACKAGE )
	private final Rotate viewRotateTransform;

	@Getter( AccessLevel.PACKAGE )
	private final Translate viewCenterTransform;

	private final EventHandler<NodeEvent> workplaneChangeHandler = _ -> updateGridFxGeometry();

	private final EventHandler<NodeEvent> designUnitChangeHandler = _ -> setDesignUnit( model.calcDesignUnit() );

	static {
		pathElementMapper = Mappers.getMapper( PathElementMapper.class );
		drawableToGeometry = new ConcurrentHashMap<>();
	}

	/**
	 * Create a new renderer. This class is intended to only be used by {@link
	 * DesignToolV3} and should not be instantiated directly otherwise except for
	 * testing purposes.
	 */
	DesignToolV3Renderer() {
		super();

		shapeScaleX = new SimpleDoubleProperty( 1.0 );
		shapeScaleY = new SimpleDoubleProperty( 1.0 );
		unitScale = new SimpleDoubleProperty( 1.0 );
		rendererCenterX = new SimpleDoubleProperty( 0.0 );
		rendererCenterY = new SimpleDoubleProperty( 0.0 );

		grid = new Pane();
		grid.getStyleClass().add( "tool-renderer-grid" );

		layers = new Pane();
		layers.getStyleClass().add( "tool-renderer-design" );

		preview = new Pane();
		preview.getStyleClass().add( "tool-renderer-preview" );

		reference = new Pane();
		reference.getStyleClass().add( "tool-renderer-reference" );

		// The world scale container
		// Contains the grid, design, preview, and reference panes
		world = new StackPane();

		// The screen scale container
		// Contains the orientation indicator
		screen = new Pane();

		// Configure the shape scale definition. The shape scale includes the unit
		// scale, DPI and the output scale and is used to modify the shape geometry.
		// shapeScale = unitScale * dpi * outputScale
		shapeScaleX.bind( unitScaleProperty().multiply( dpiXProperty() ).multiply( outputScaleXProperty() ) );
		shapeScaleY.bind( unitScaleProperty().multiply( dpiYProperty() ).multiply( outputScaleYProperty() ) );

		// Create and set the world transforms
		viewZoomTransform = new Scale( 1, -1 );
		viewRotateTransform = new Rotate( 0, 0, 0 );
		viewCenterTransform = new Translate( 0, 0 );
		world.getTransforms().setAll( viewZoomTransform, viewRotateTransform, viewCenterTransform );

		// Configure the renderer center definition. The renderer center maintains
		// the center point in the parent coordinate system regardless of the parent
		// size, view zoom or output scale. This is important when converting
		// between screen and world coordinates.
		rendererCenterX.bind( widthProperty().multiply( 0.5 ).multiply( outputScaleXProperty() ).divide( viewZoomXProperty() ) );
		rendererCenterY.bind( heightProperty().multiply( -0.5 ).multiply( outputScaleYProperty() ).divide( viewZoomYProperty() ) );

		// The rotation transform needs to include the rotation angle and the pivot
		// point. The pivot point is always in parent coordinates and is bound to
		// the renderer center.
		viewRotateTransform.angleProperty().bind( viewRotateProperty() );
		viewRotateTransform.pivotXProperty().bind( getRendererCenterX() );
		viewRotateTransform.pivotYProperty().bind( getRendererCenterY() );

		// The zoom transform does not include the DPI property because the geometry
		// values already include the DPI. What is interesting here is that we divide
		// out the output scale at the same time. This allows JavaFX to render the
		// geometry at the highest resolution, regardless of the output scale set by
		// the user. Someday this may need to be tied to a HiDPI setting, but we'll
		// leave it here to understand how the technique works.
		// viewZoomTransform = viewZoom / outputScale;
		viewZoomTransform.xProperty().bind( viewZoomXProperty().divide( outputScaleXProperty() ) );
		viewZoomTransform.yProperty().bind( viewZoomYProperty().divide( outputScaleYProperty() ).negate() );

		// The translation properties do not include the output scale property because
		// these are parent coordinates and not local coordinates, and the parent
		// transforms have already incorporated the output scale. The translation
		// properties also have to compensate for the scale acting at the center of
		// the pane and not at the origin.
		viewCenterTransform.xProperty().bind( getRendererCenterX().subtract( viewCenterXProperty().multiply( shapeScaleXProperty() ) ) );
		viewCenterTransform.yProperty().bind( getRendererCenterY().subtract( viewCenterYProperty().multiply( shapeScaleYProperty() ) ) );

		// Update the design geometry when the global scale changes
		// TODO Consider changing the grid geometry to bound properties
		shapeScaleXProperty().addListener( ( _, _, _ ) -> this.updateGridFxGeometry() );
		shapeScaleYProperty().addListener( ( _, _, _ ) -> this.updateGridFxGeometry() );

		// Important: Adding the children last ensures that they use all the values
		// set above. This fixes a bug where the children were added earlier in the
		// method causing them to use incorrect values of zero for width and height.
		world.getChildren().addAll( grid, layers, preview, reference );
		getChildren().addAll( world, screen );
	}

	/**
	 * Set the select aperture. Design tools should set the select aperture to
	 * one of a set of predefined apertures. Constantly using new objects for the
	 * aperture will reduce performance. Tools should reuse the same aperture
	 * instances as much as possible. Once the aperture shape is set, tools are
	 * free to update the shape outside the renderer and the renderer will update
	 * it according. Only change the select aperture when changing selection
	 * modes, such as from point selection to box selection.
	 *
	 * @param aperture The select aperture.
	 */
	@Override
	public void setSelectAperture( DesignShape aperture ) {
		super.setSelectAperture( aperture );

		// This implementation choice requires some special attention to the shapes
		// being passed to this method. In particular, it is poor practice to
		// constantly send new design shape objects to this method. Under normal
		// circumstances, only three apertures should be used, the default aperture,
		// the point select aperture and the box select aperture. This method is
		// implemented this way to not restrict future other apertures, but caution
		// should be taken to ensure that the shapes are not constantly new objects.
		if( aperture != null ) mapDesignShape( aperture );

		if( aperture == null || aperture == DEFAULT_SELECT_APERTURE ) {
			// Remove geometry
			reference.getChildren().removeAll( DEFAULT_SELECT_APERTURE.getFxShape() );
		} else {
			reference.getChildren().addAll( aperture.getFxShape() );
			System.out.println( "Aperture added to reference layer" );
		}
	}

	@Override
	public Design<? extends DesignModel> getDesign() {
		return design;
	}

	@Override
	public void setDesign( Design<? extends DesignModel> design ) {
		if( this.design == design ) return;

		this.design = design;
		if( this.design == null ) {
			setDesignModel( null );
		} else {
			setDesignModel( design.getDataModel() );

			// Map the resource and preview layers
			mapDesignLayer( design.getDesignContext().getPreviewLayer(), preview, false );
			mapDesignLayer( design.getDesignContext().getReferenceLayer(), reference, false );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	private void setDesignModel( DesignModel design ) {
		if( this.model != null ) {
			this.model.unregister( this, DesignModel.UNIT, designUnitChangeHandler );
		}

		this.model = design;

		if( this.model != null ) {
			design.register( this, DesignModel.UNIT, designUnitChangeHandler );
			setDesignUnit( design.calcDesignUnit() );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Workplane getWorkplane() {
		return workplane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkplane( Workplane workplane ) {
		if( this.workplane != null ) {
			this.workplane.unregister( this, NodeEvent.ANY, workplaneChangeHandler );
		}

		this.workplane = workplane;

		if( this.workplane != null ) {
			this.workplane.register( this, NodeEvent.ANY, workplaneChangeHandler );
		}

		updateGridFxGeometry();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGridVisible() {
		return grid.isVisible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridVisible( boolean visible ) {
		// This method has a very important implementation, it is more than just
		// setting a flag, it participates in the performance of the renderer by
		// creating and destroying geometry. Grid geometry is only created when
		// needed, and that is when the grid is made visible. The same happens in
		// reverse; when the grid is hidden, the geometry is not needed anymore.
		if( visible ) {
			updateGridFxGeometry();
			grid.setVisible( true );
		} else {
			grid.setVisible( false );
			grid.getChildren().clear();
		}
	}

	@Override
	public BooleanProperty gridVisible() {
		return grid.visibleProperty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLayerVisible( DesignLayer layer, boolean visible ) {
		// This method has a very important implementation, it is more than just
		// setting a flag, it participates in the performance of the renderer by
		// creating and destroying geometry. Since most layers are not visible in
		// most designs, layer geometry is only created when needed, and that is
		// most often when the layer is made visible. The same happens in reverse;
		// when the layer is hidden, the geometry is usually not needed anymore.
		if( visible ) {
			// Add the FX layer to the renderer
			Pane pane = mapDesignLayer( layer );
			layers.getChildren().add( determineLayerIndex( layer ), pane );
		} else {
			// Remove the FX layer from the renderer
			Pane pane = getFxGeometry( layer );
			if( pane != null ) layers.getChildren().remove( pane );
			layer.setValue( FX_GEOMETRY, null );
		}

		super.setLayerVisible( layer, visible );
	}

	// Maintain the super implementation
	// public List<DesignLayer> getVisibleLayers()

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisibleLayers( @NonNull Collection<DesignLayer> layers ) {
		if( this.model == null ) return;

		// Optimization: show only the specified layers; hide all others currently visible

		// Hide layers that are currently visible and not in the target collection
		getVisibleLayers().forEach( existing -> {
			if( !layers.contains( existing ) ) setLayerVisible( existing, false );
		} );

		// Show any requested layers that are not already visible
		layers.forEach( layer -> {
			if( !isLayerVisible( layer ) ) setLayerVisible( layer, true );
		} );

		super.setVisibleLayers( layers );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render() {
		// Should not need this method for the V3 renderer.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print( double factor ) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transform getScreenToWorldTransform() {
		try {
			return getWorldToScreenTransform().createInverse();
		} catch( NonInvertibleTransformException exception ) {
			// This should never happen since the world-to-screen transform should always be invertible
			throw new RuntimeException( exception );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D screenToWorld( double x, double y ) {
		return screenToWorld( new Point2D( x, y ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D screenToWorld( Point2D point ) {
		return getScreenToWorldTransform().transform( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorld( double x, double y, double z ) {
		return screenToWorld( new Point3D( x, y, z ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorld( Point3D point ) {
		return getScreenToWorldTransform().transform( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bounds screenToWorld( Bounds bounds ) {
		return getScreenToWorldTransform().transform( bounds );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transform getWorldToScreenTransform() {
		return world.getLocalToParentTransform().createConcatenation( Transform.scale( getShapeScaleX(), getShapeScaleY() ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D worldToScreen( double x, double y ) {
		return worldToScreen( new Point2D( x, y ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D worldToScreen( Point2D point ) {
		return getWorldToScreenTransform().transform( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D worldToScreen( double x, double y, double z ) {
		return worldToScreen( new Point3D( x, y, z ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D worldToScreen( Point3D point ) {
		return getWorldToScreenTransform().transform( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bounds worldToScreen( Bounds bounds ) {
		return getWorldToScreenTransform().transform( bounds );
	}

	final Pane layersPane() {
		return layers;
	}

	Bounds getVisualBounds( Node node ) {
		return node.getBoundsInParent();
	}

	double getShapeScaleX() {
		return shapeScaleX.get();
	}

	private DoubleProperty shapeScaleXProperty() {
		return shapeScaleX;
	}

	double getShapeScaleY() {
		return shapeScaleY.get();
	}

	private DoubleProperty shapeScaleYProperty() {
		return shapeScaleY;
	}

	double getUnitScale() {
		return unitScale.get();
	}

	void setUnitScale( double unitScale ) {
		this.unitScale.set( unitScale );
	}

	private DoubleProperty unitScaleProperty() {
		return unitScale;
	}

	void setDesignUnit( DesignUnit unit ) {
		setUnitScale( unit.to( 1, DesignUnit.IN ) );
	}

	@Note( Note.THREAD_SAFE )
	void updateGridFxGeometry() {
		// Get a local reference for thread safety
		final Workplane workplane = this.workplane;

		Fx.onFxOrCurrent( () -> {
			if( workplane == null ) {
				grid.getChildren().clear();
			} else {
				workplane.getGridSystem().updateFxGeometryGrid( workplane, getShapeScaleX(), grid.getChildren() );
			}
		} );
	}

	/**
	 * Determines the appropriate index for placing a design layer among the existing
	 * FX layers based on the order of the design layers in the design.
	 *
	 * @param designLayer The design layer to determine the index for.
	 * @return The computed index where the design layer should be inserted among FX layers.
	 */
	private int determineLayerIndex( DesignLayer designLayer ) {
		List<DesignLayer> designLayers = new ArrayList<>( model.getLayers().getAllLayers() );
		Collections.reverse( designLayers );
		List<Node> fxLayers = layers.getChildren();

		// Determine the appropriate index in the FX layers
		int index = -1;
		for( DesignLayer checkLayer : designLayers ) {
			if( checkLayer == designLayer ) break;
			Pane fxLayer = getFxGeometry( checkLayer );
			if( fxLayer != null ) index = fxLayers.indexOf( fxLayer );
		}

		return index + 1;
	}

	/**
	 * Get the design layer index from the existing FX layer panes.
	 *
	 * @param layer The design layer to get the index for.
	 * @return The index of the design layer in the existing FX layer panes.
	 */
	@Note( Note.TESTING_ONLY )
	int getPaneIndex( DesignLayer layer ) {
		Node node = getFxGeometry( layer );
		return layers.getChildren().indexOf( node );
	}

	private Pane mapDesignLayer( DesignLayer designLayer ) {
		return mapDesignLayer( designLayer, true );
	}

	private Pane mapDesignLayer( DesignLayer designLayer, boolean includeSubLayers ) {
		return mapDesignLayer( designLayer, new Pane(), includeSubLayers );
	}

	private Pane mapDesignLayer( DesignLayer designLayer, Pane pane, boolean includeSubLayers ) {
		// Link the DesignLayer and Pane references
		putFxGeometry( designLayer, pane );
		pane.setUserData( designLayer );

		// Register event handlers for the DesignLayer
		designLayer.register(
			pane, NodeEvent.ANY, e -> {
				if( e.getEventType() == NodeEvent.NODE_CHANGED ) return;
				if( e.getEventType() == NodeEvent.VALUE_CHANGED ) return;

				if( e.getEventType() == NodeEvent.CHILD_ADDED ) {
					DesignShape shape = e.getNewValue();
					Shape fxShape = mapDesignShape( shape );
					putFxGeometry( shape, fxShape );
					Fx.run( () -> pane.getChildren().add( fxShape ) );
				} else if( e.getEventType() == NodeEvent.CHILD_REMOVED ) {
					Shape shape = getFxGeometry( e.getOldValue() );
					Fx.run( () -> pane.getChildren().remove( shape ) );
				}
			}
		);

		designLayer.getShapes().forEach( shape -> {
			Shape fxShape = mapDesignShape( shape );
			putFxGeometry( shape, fxShape );
			pane.getChildren().add( fxShape );

			// TODO Handlers need to be attached with the pane as owner
			// i.e. designLayer.register(layer, "order", e -> changeLayerOrder() );
		} );

		if( includeSubLayers ) {
			designLayer.getLayers().forEach( subLayer -> pane.getChildren().add( mapDesignLayer( subLayer ) ) );
		}

		return pane;
	}

	private Shape mapDesignShape( DesignShape designShape ) {
		Shape fxShape = getFxGeometry( designShape );

		// If an FX shape is already bound, don't do it again
		if( fxShape != null ) return fxShape;

		fxShape = switch( designShape.getType() ) {
			case ARC -> bindArcGeometry( (DesignArc)designShape );
			case BOX -> bindBoxGeometry( (DesignBox)designShape );
			case CUBIC -> bindCubicGeometry( (DesignCubic)designShape );
			case ELLIPSE -> bindEllipseGeometry( (DesignEllipse)designShape );
			case LINE -> bindLineGeometry( (DesignLine)designShape );
			case MARKER -> bindMarkerGeometry( (DesignMarker)designShape );
			case PATH -> bindPathGeometry( (DesignPath)designShape );
			case QUAD -> bindQuadGeometry( (DesignQuad)designShape );
			case TEXT -> bindTextGeometry( (DesignText)designShape );
		};

		fxShape.setManaged( false );
		fxShape.setUserData( designShape );

		return fxShape;
	}

	@SuppressWarnings( "unchecked" )
	private <T> T getFxGeometry( DesignDrawable drawable ) {
		return (T)drawableToGeometry.get( new GeometryKey( this, drawable ) );
	}

	/**
	 * Create the map of geometry by renderer if needed
	 *
	 * @param drawable The design drawable to create the map for.
	 * @param node The FX node to link to the map.
	 */
	private void putFxGeometry( DesignDrawable drawable, Node node ) {
		drawableToGeometry.put( new GeometryKey( this, drawable ), node );
	}

	// TODO Finish building the bind methods for the remaining design shapes
	// arc - done
	// box - done
	// cubic - done
	// ellipse - done
	// line - done
	// marker - done
	// offset
	// path - done
	// quad - done
	// text - done

	private Arc bindArcGeometry( DesignArc designArc ) {
		Arc arc = new Arc();

		bindCommonShapeGeometry( designArc, arc );

		DesignDoubleBinding originXProperty = new DesignDoubleBinding( designArc, DesignArc.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding originYProperty = new DesignDoubleBinding( designArc, DesignArc.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding radiusXProperty = new DesignDoubleBinding( designArc, DesignArc.RADII, v -> v.getRadii().getX() );
		DesignDoubleBinding radiusYProperty = new DesignDoubleBinding( designArc, DesignArc.RADII, v -> v.getRadii().getY() );
		DesignDoubleBinding startAngleProperty = new DesignDoubleBinding( designArc, DesignArc.START, DesignArc::calcStart );
		DesignDoubleBinding lengthProperty = new DesignDoubleBinding( designArc, DesignArc.EXTENT, DesignArc::calcExtent );
		DesignDoubleBinding rotateProperty = new DesignDoubleBinding( designArc, DesignArc.ROTATE, DesignArc::calcRotate );

		arc.centerXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		arc.centerYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		arc.radiusXProperty().bind( shapeScaleXProperty().multiply( radiusXProperty ) );
		arc.radiusYProperty().bind( shapeScaleYProperty().multiply( radiusYProperty ) );
		arc.startAngleProperty().bind( startAngleProperty.negate() );
		arc.lengthProperty().bind( lengthProperty.negate() );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateProperty );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		arc.getTransforms().setAll( rotate );

		return arc;
	}

	private Rectangle bindBoxGeometry( DesignBox designBox ) {
		Rectangle box = new Rectangle();

		bindCommonShapeGeometry( designBox, box );

		DesignDoubleBinding originXProperty = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding originYProperty = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding widthProperty = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize().getX() );
		DesignDoubleBinding heightProperty = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize().getY() );
		DesignDoubleBinding rotateProperty = new DesignDoubleBinding( designBox, DesignBox.ROTATE, DesignBox::calcRotate );

		box.xProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		box.yProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		box.widthProperty().bind( shapeScaleXProperty().multiply( widthProperty ) );
		box.heightProperty().bind( shapeScaleYProperty().multiply( heightProperty ) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateProperty );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		box.getTransforms().setAll( rotate );

		return box;
	}

	private CubicCurve bindCubicGeometry( DesignCubic designCubic ) {
		CubicCurve quad = new CubicCurve();

		bindCommonShapeGeometry( designCubic, quad );

		DesignDoubleBinding startXProperty = new DesignDoubleBinding( designCubic, DesignQuad.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding startYProperty = new DesignDoubleBinding( designCubic, DesignQuad.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding originControlXProperty = new DesignDoubleBinding( designCubic, DesignQuad.CONTROL, v -> v.getOriginControl().getX() );
		DesignDoubleBinding originControlYProperty = new DesignDoubleBinding( designCubic, DesignQuad.CONTROL, v -> v.getOriginControl().getY() );
		DesignDoubleBinding pointControlXProperty = new DesignDoubleBinding( designCubic, DesignQuad.POINT, v -> v.getPointControl().getX() );
		DesignDoubleBinding pointControlYProperty = new DesignDoubleBinding( designCubic, DesignQuad.POINT, v -> v.getPointControl().getY() );
		DesignDoubleBinding pointXProperty = new DesignDoubleBinding( designCubic, DesignQuad.POINT, v -> v.getPoint().getX() );
		DesignDoubleBinding pointYProperty = new DesignDoubleBinding( designCubic, DesignQuad.POINT, v -> v.getPoint().getY() );

		quad.startXProperty().bind( shapeScaleXProperty().multiply( startXProperty ) );
		quad.startYProperty().bind( shapeScaleYProperty().multiply( startYProperty ) );
		quad.controlX1Property().bind( shapeScaleXProperty().multiply( originControlXProperty ) );
		quad.controlY1Property().bind( shapeScaleYProperty().multiply( originControlYProperty ) );
		quad.controlX2Property().bind( shapeScaleXProperty().multiply( pointControlXProperty ) );
		quad.controlY2Property().bind( shapeScaleYProperty().multiply( pointControlYProperty ) );
		quad.endXProperty().bind( shapeScaleXProperty().multiply( pointXProperty ) );
		quad.endYProperty().bind( shapeScaleYProperty().multiply( pointYProperty ) );

		return quad;
	}

	private Ellipse bindEllipseGeometry( DesignEllipse designEllipse ) {
		Ellipse ellipse = new Ellipse();

		bindCommonShapeGeometry( designEllipse, ellipse );

		DesignDoubleBinding originXProperty = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding originYProperty = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding radiusXProperty = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii().getX() );
		DesignDoubleBinding radiusYProperty = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii().getY() );
		DesignDoubleBinding rotateProperty = new DesignDoubleBinding( designEllipse, DesignEllipse.ROTATE, DesignEllipse::calcRotate );

		ellipse.centerXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		ellipse.centerYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		ellipse.radiusXProperty().bind( shapeScaleXProperty().multiply( radiusXProperty ) );
		ellipse.radiusYProperty().bind( shapeScaleYProperty().multiply( radiusYProperty ) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateProperty );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );
		ellipse.getTransforms().setAll( rotate );

		return ellipse;
	}

	private Line bindLineGeometry( DesignLine designLine ) {
		Line line = new Line();

		bindCommonShapeGeometry( designLine, line );

		DesignDoubleBinding startXProperty = new DesignDoubleBinding( designLine, DesignLine.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding startYProperty = new DesignDoubleBinding( designLine, DesignLine.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding pointXProperty = new DesignDoubleBinding( designLine, DesignLine.POINT, v -> v.getPoint().getX() );
		DesignDoubleBinding pointYProperty = new DesignDoubleBinding( designLine, DesignLine.POINT, v -> v.getPoint().getY() );

		line.startXProperty().bind( shapeScaleXProperty().multiply( startXProperty ) );
		line.startYProperty().bind( shapeScaleYProperty().multiply( startYProperty ) );
		line.endXProperty().bind( shapeScaleXProperty().multiply( pointXProperty ) );
		line.endYProperty().bind( shapeScaleYProperty().multiply( pointYProperty ) );

		return line;
	}

	private Path bindMarkerGeometry( DesignMarker designMarker ) {
		Path path = new Path();

		bindCommonShapeGeometry( designMarker, path );
		path.setFillRule( FillRule.EVEN_ODD );

		// Bind on steps and update the path geometry
		DesignBinding<List<DesignPath.Step>> stepsBinding = new DesignBinding<>( designMarker, DesignPath.STEPS, DesignMarker::getSteps );
		ObjectBinding<List<PathElement>> elementsBinding = Bindings.createObjectBinding(
			() -> {
				List<PathElement> elements = new ArrayList<>();
				double shapeScaleX = shapeScaleXProperty().get();
				double shapeScaleY = shapeScaleYProperty().get();
				for( DesignPath.Step step : stepsBinding.get() ) {
					elements.add( pathElementMapper.map( step, shapeScaleX, shapeScaleY ) );
				}
				return elements;
			}, stepsBinding
		);
		path.getElements().setAll( elementsBinding.get() );
		elementsBinding.addListener( ( _, _, n ) -> path.getElements().setAll( n ) );

		return path;
	}

	private Path bindPathGeometry( DesignPath designPath ) {
		Path path = new Path();

		bindCommonShapeGeometry( designPath, path );

		// Bind on steps and update the path geometry
		DesignBinding<List<DesignPath.Step>> stepsBinding = new DesignBinding<>( designPath, DesignPath.STEPS, DesignPath::getSteps );
		ObjectBinding<List<PathElement>> elementsBinding = Bindings.createObjectBinding(
			() -> {
				List<PathElement> elements = new ArrayList<>();
				double shapeScaleX = shapeScaleXProperty().get();
				double shapeScaleY = shapeScaleYProperty().get();
				for( DesignPath.Step step : stepsBinding.get() ) {
					elements.add( pathElementMapper.map( step, shapeScaleX, shapeScaleY ) );
				}
				return elements;
			}, stepsBinding
		);
		path.getElements().setAll( elementsBinding.get() );
		elementsBinding.addListener( ( _, _, n ) -> path.getElements().setAll( n ) );

		return path;
	}

	private QuadCurve bindQuadGeometry( DesignQuad designQuad ) {
		QuadCurve quad = new QuadCurve();

		bindCommonShapeGeometry( designQuad, quad );

		DesignDoubleBinding startXProperty = new DesignDoubleBinding( designQuad, DesignQuad.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding startYProperty = new DesignDoubleBinding( designQuad, DesignQuad.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding controlXProperty = new DesignDoubleBinding( designQuad, DesignQuad.CONTROL, v -> v.getControl().getX() );
		DesignDoubleBinding controlYProperty = new DesignDoubleBinding( designQuad, DesignQuad.CONTROL, v -> v.getControl().getY() );
		DesignDoubleBinding pointXProperty = new DesignDoubleBinding( designQuad, DesignQuad.POINT, v -> v.getPoint().getX() );
		DesignDoubleBinding pointYProperty = new DesignDoubleBinding( designQuad, DesignQuad.POINT, v -> v.getPoint().getY() );

		quad.startXProperty().bind( shapeScaleXProperty().multiply( startXProperty ) );
		quad.startYProperty().bind( shapeScaleYProperty().multiply( startYProperty ) );
		quad.controlXProperty().bind( shapeScaleXProperty().multiply( controlXProperty ) );
		quad.controlYProperty().bind( shapeScaleYProperty().multiply( controlYProperty ) );
		quad.endXProperty().bind( shapeScaleXProperty().multiply( pointXProperty ) );
		quad.endYProperty().bind( shapeScaleYProperty().multiply( pointYProperty ) );

		return quad;
	}

	// Eventually this should only have to be called once per design shape
	private Text bindTextGeometry( DesignText designText ) {
		Text text = new Text();

		bindCommonShapeGeometry( designText, text );

		DesignDoubleBinding originXProperty = new DesignDoubleBinding( designText, DesignText.ORIGIN, v -> v.getOrigin().getX() );
		DesignDoubleBinding originYProperty = new DesignDoubleBinding( designText, DesignText.ORIGIN, v -> v.getOrigin().getY() );
		DesignDoubleBinding rotateProperty = new DesignDoubleBinding( designText, DesignText.ROTATE, DesignShape::calcRotate );
		DesignBinding<String> textProperty = new DesignBinding<>( designText, DesignText.TEXT, DesignText::getText );
		DesignBinding<String> fontNameProperty = new DesignBinding<>( designText, DesignText.FONT_NAME, DesignText::getFontName );
		DesignBinding<FontWeight> fontWeightProperty = new DesignBinding<>( designText, DesignText.FONT_WEIGHT, DesignText::calcFontWeight );
		DesignBinding<FontPosture> fontPostureProperty = new DesignBinding<>( designText, DesignText.FONT_POSTURE, DesignText::calcFontPosture );
		DesignDoubleBinding textSizeProperty = new DesignDoubleBinding( designText, DesignText.TEXT_SIZE, DesignText::calcTextSize );

		text.textProperty().bind( textProperty );

		text.xProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		text.yProperty().bind( shapeScaleYProperty().multiply( originYProperty ).negate() );

		text.fontProperty().bind( Bindings.createObjectBinding(
			() -> Font.font( fontNameProperty.get(), designText.calcFontWeight(), designText.calcFontPosture(), textSizeProperty.get() * shapeScaleYProperty().get() ),
			fontNameProperty,
			fontWeightProperty,
			fontPostureProperty,
			textSizeProperty,
			shapeScaleYProperty()
		) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateProperty );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXProperty ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYProperty ) );

		// Rotate must be before scale
		text.getTransforms().setAll( rotate, Transform.scale( 1, -1 ) );

		return text;
	}

	/**
	 * Bind the common geometry properties of the shape. This method is used to
	 * bind the common shape properties to their dependent properties, whether
	 * they be FX properties or design properties.
	 *
	 * @param designShape The source design shape
	 * @param shape The target FX shape
	 */
	private void bindCommonShapeGeometry( DesignShape designShape, Shape shape ) {
		shape.fillProperty().bind( new DesignBinding<>( designShape, DesignShape.FILL_PAINT, DesignShape::calcFillPaint ) );
		shape.strokeProperty().bind( new DesignBinding<>( designShape, DesignShape.DRAW_PAINT, DesignShape::calcDrawPaint ) );
		shape.strokeWidthProperty().bind( shapeScaleXProperty().multiply( new DesignDoubleBinding( designShape, DesignShape.DRAW_WIDTH, DesignShape::calcDrawWidth ) ) );
		shape.strokeLineCapProperty().bind( new DesignBinding<>( designShape, DesignShape.DRAW_CAP, DesignShape::calcDrawCap ) );
		shape.strokeLineJoinProperty().bind( new DesignBinding<>( designShape, DesignShape.DRAW_JOIN, DesignShape::calcDrawJoin ) );
		//shape.strokeTypeProperty().bind( new DesignBinding<>( designShape, DesignShape.DRAW_TYPE, DesignShape::calcDrawType ) );
		//shape.strokeMiterLimitProperty().bind( shapeScaleXProperty().multiply( new DesignDoubleBinding( designShape, DesignShape.DRAW_MITER_LIMIT, DesignShape::calcDrawMiterLimit ) ) );

		// Dash offset
		shape.strokeDashOffsetProperty().bind( shapeScaleXProperty().multiply( new DesignDoubleBinding( designShape, DesignShape.DASH_OFFSET, DesignShape::calcDashOffset ) ) );
		// Dash pattern
		DesignBinding<List<Double>> patternBinding = new DesignBinding<>( designShape, DesignShape.DASH_PATTERN, DesignShape::calcDashPattern );
		ObjectBinding<List<Double>> dashBinding = Bindings.createObjectBinding(
			() -> patternBinding.get().stream().map( d -> d * shapeScaleXProperty().get() ).toList(),
			shapeScaleXProperty(),
			patternBinding
		);
		shape.getStrokeDashArray().setAll( dashBinding.get() );
		dashBinding.addListener( ( _, _, n ) -> shape.getStrokeDashArray().setAll( n ) );
	}

	private record GeometryKey(DesignRenderer renderer, DesignDrawable drawable) {}

}
