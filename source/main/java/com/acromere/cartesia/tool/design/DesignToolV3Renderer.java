package com.acromere.cartesia.tool.design;

import com.acromere.annotation.Note;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.DesignValue;
import com.acromere.cartesia.data.*;
import com.acromere.cartesia.tool.Workplane;
import com.acromere.cartesia.tool.design.binding.*;
import com.acromere.data.DataNodeEvent;
import com.acromere.event.EventHandler;
import com.acromere.zerra.javafx.Fx;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberBinding;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

	private static final PathElementMapper pathElementMapper;

	/**
	 * Caution: This map is shared among all renderers of this type. This could
	 * result in a memory leak if the FX geometry is not properly removed from the
	 * renderers when drawables are removed from the design, tools are closed,
	 * etc. Watch for memory leaks.
	 */
	private final Map<GeometryKey, Node> drawableToGeometry;

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

	/**
	 * The aperture geometry layer.
	 */
	@Getter
	final Pane aperture;

	private final DoubleProperty apertureUnitScale;

	private final DoubleProperty apertureShapeScaleX;

	private final DoubleProperty apertureShapeScaleY;

	private final DoubleProperty designUnitScale;

	private final DoubleProperty designShapeScaleX;

	private final DoubleProperty designShapeScaleY;

	@Getter( AccessLevel.PACKAGE )
	private final DoubleProperty rendererCenterX;

	@Getter( AccessLevel.PACKAGE )
	private final DoubleProperty rendererCenterY;

	@Getter( AccessLevel.PACKAGE )
	private final Scale viewScaleTransform;

	@Getter( AccessLevel.PACKAGE )
	private final Rotate viewRotateTransform;

	@Getter( AccessLevel.PACKAGE )
	private final Translate viewCenterTransform;

	private final EventHandler<DataNodeEvent> workplaneChangeHandler = _ -> updateGridFxGeometry();

	private final EventHandler<DataNodeEvent> designUnitChangeHandler = _ -> setDesignUnit( model.calcDesignUnit() );

	static {
		pathElementMapper = Mappers.getMapper( PathElementMapper.class );
	}

	/**
	 * Create a new renderer. This class is intended to only be used by {@link
	 * DesignToolV3} and should not be instantiated directly otherwise except for
	 * testing purposes.
	 */
	DesignToolV3Renderer() {
		super();

		drawableToGeometry = new ConcurrentHashMap<>();

		apertureUnitScale = new SimpleDoubleProperty( 1.0 );
		apertureShapeScaleX = new SimpleDoubleProperty( 1.0 );
		apertureShapeScaleY = new SimpleDoubleProperty( 1.0 );

		designUnitScale = new SimpleDoubleProperty( 1.0 );
		designShapeScaleX = new SimpleDoubleProperty( 1.0 );
		designShapeScaleY = new SimpleDoubleProperty( 1.0 );

		rendererCenterX = new SimpleDoubleProperty( 0.0 );
		rendererCenterY = new SimpleDoubleProperty( 0.0 );

		grid = new Pane();
		grid.getStyleClass().add( "tool-renderer-grid" );

		layers = new Pane();
		layers.getStyleClass().add( "tool-renderer-design" );

		reference = new Pane();
		reference.getStyleClass().add( "tool-renderer-reference" );

		preview = new Pane();
		preview.getStyleClass().add( "tool-renderer-preview" );

		aperture = new Pane();
		aperture.getStyleClass().add( "tool-renderer-aperture" );

		// The world scale container
		// Contains the grid, design, preview, and reference panes
		world = new StackPane();

		// The screen scale container
		// Contains the aperture and orientation indicator panes
		screen = new StackPane();

		// Configure the shape scale definition. The shape scale includes the
		// aperture unit scale, DPI and the output scale and is used to modify the
		// shape geometry.
		// shapeScale = apertureUnitScale * dpi * outputScale
		apertureShapeScaleX.bind( apertureUnitScaleProperty().multiply( dpiXProperty() ).multiply( outputScaleXProperty() ) );
		apertureShapeScaleY.bind( apertureUnitScaleProperty().multiply( dpiYProperty() ).multiply( outputScaleYProperty() ) );

		// Configure the shape scale definition. The shape scale includes the
		// design unit scale, DPI and the output scale and is used to modify the
		// shape geometry.
		// shapeScale = designUnitScale * dpi * outputScale
		designShapeScaleX.bind( designUnitScaleProperty().multiply( dpiXProperty() ).multiply( outputScaleXProperty() ) );
		designShapeScaleY.bind( designUnitScaleProperty().multiply( dpiYProperty() ).multiply( outputScaleYProperty() ) );

		// Create and set the world transforms
		viewScaleTransform = new Scale( 1, -1 );
		viewRotateTransform = new Rotate( 0, 0, 0 );
		viewCenterTransform = new Translate( 0, 0 );
		world.getTransforms().setAll( viewScaleTransform, viewRotateTransform, viewCenterTransform );
		aperture.getTransforms().setAll( viewScaleTransform, viewRotateTransform, viewCenterTransform );

		// Configure the renderer center definition. The renderer center maintains
		// the center point in the parent coordinate system regardless of the parent
		// size, view zoom or output scale. This is important when converting
		// between screen and world coordinates.
		rendererCenterX.bind( widthProperty().multiply( 0.5 ).multiply( outputScaleXProperty() ).divide( viewZoomXProperty() ) );
		rendererCenterY.bind( heightProperty().multiply( -0.5 ).multiply( outputScaleYProperty() ).divide( viewZoomYProperty() ) );

		// The zoom transform does not include the DPI property because the geometry
		// values already include the DPI. What is interesting here is that we divide
		// out the output scale at the same time. This allows JavaFX to render the
		// geometry at the highest resolution, regardless of the output scale set by
		// the user. Someday this may need to be tied to a HiDPI setting, but we'll
		// leave it here to understand how the technique works.
		// viewZoomTransform = viewZoom / outputScale;
		viewScaleTransform.xProperty().bind( viewZoomXProperty().divide( outputScaleXProperty() ) );
		viewScaleTransform.yProperty().bind( viewZoomYProperty().divide( outputScaleYProperty() ).negate() );

		// The rotation transform needs to include the rotation angle and the pivot
		// point. The pivot point is always in parent coordinates and is bound to
		// the renderer center.
		viewRotateTransform.angleProperty().bind( viewRotateProperty() );
		viewRotateTransform.pivotXProperty().bind( getRendererCenterX() );
		viewRotateTransform.pivotYProperty().bind( getRendererCenterY() );

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
		screen.getChildren().addAll( aperture );
		getChildren().addAll( world, screen );

		this.aperture.getChildren().add( mapSelectAperture( POINT_SELECT_APERTURE ) );
		this.aperture.getChildren().add( mapSelectAperture( WINDOW_SELECT_APERTURE ) );
	}

	double getApertureUnitScale() {
		return apertureUnitScale.get();
	}

	void setApertureUnitScale( double apertureUnitScale ) {
		this.apertureUnitScale.set( apertureUnitScale );
	}

	private DoubleProperty apertureUnitScaleProperty() {
		return apertureUnitScale;
	}

	/**
	 * Set the select aperture value. The select aperture value is the human value
	 * for the select aperture, commonly in millimeters or inches. This method
	 * uses the select aperture value to set the aperture unit scale, in DPI units,
	 * used to render the aperture geometry.
	 *
	 * @param value The select aperture value.
	 */
	public void setSelectTolerance( DesignValue value ) {
		setApertureUnitScale( value.unit().to( 1, DesignUnit.IN ) );
	}

	//	/**
	//	 * Set the select aperture. Design tools should set the select aperture to
	//	 * one of a set of predefined apertures. Constantly using new objects for the
	//	 * aperture will reduce performance. Tools should reuse the same aperture
	//	 * instances as much as possible. Once the aperture shape is set, tools are
	//	 * free to update the shape outside the renderer and the renderer will update
	//	 * it according. Only change the select aperture when changing selection
	//	 * modes, such as from point selection to box selection.
	//	 *
	//	 * @param aperture The select aperture.
	//	 */
	//	@Override
	//	public void setSelectAperture( DesignShape aperture ) {
	//		super.setSelectAperture( aperture );
	//
	//		// This implementation choice requires some special attention to the shapes
	//		// being passed to this method. In particular, it is poor practice to
	//		// constantly send new design shape objects to this method. Under normal
	//		// circumstances, only three apertures should be used, the default aperture,
	//		// the point select aperture and the box select aperture. This method is
	//		// implemented this way to not restrict future other apertures, but caution
	//		// should be taken to ensure that the shapes are not constantly new objects.
	//
	//		if( aperture == null ) {
	//			this.aperture.getChildren().remove( getFxGeometry( getSelectAperture() ) );
	//		} else {
	//			this.aperture.getChildren().add( getFxGeometry( aperture ) );
	//		}
	//	}

	@Override
	public Design<? extends DesignModel> getDesign() {
		return design;
	}

	@Override
	public void setDesign( Design<? extends DesignModel> design ) {
		if( this.design == design ) return;

		if( this.design != null ) {
			// Clear the design model
			setDesignModel( null );

			// Clear the resource and preview layers
			preview.getChildren().clear();
			reference.getChildren().clear();
		}

		this.design = design;

		if( this.design != null ) {
			// Set the design model
			setDesignModel( design.getDataModel() );

			// Map the resource and preview layers
			mapDesignLayer( design.getDesignContext().getPreviewLayer(), preview, false );
			mapDesignLayer( design.getDesignContext().getReferenceLayer(), reference, false );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	private void setDesignModel( DesignModel model ) {
		if( this.model != null ) {
			this.model.unregister( this, DesignModel.UNIT, designUnitChangeHandler );
			setDesignUnit( DesignModel.DEFAULT_DESIGN_UNIT );
		}

		this.model = model;

		if( this.model != null ) {
			model.register( this, DesignModel.UNIT, designUnitChangeHandler );
			setDesignUnit( model.calcDesignUnit() );
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
			this.workplane.unregister( this, DataNodeEvent.ANY, workplaneChangeHandler );
		}

		this.workplane = workplane;

		if( this.workplane != null ) {
			this.workplane.register( this, DataNodeEvent.ANY, workplaneChangeHandler );
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
			Fx.run( () -> layers.getChildren().add( determineLayerIndex( layer ), pane ) );
		} else {
			// Remove the FX layer from the renderer
			Pane pane = getFxGeometry( layer );
			if( pane != null ) Fx.run( () -> layers.getChildren().remove( pane ) );
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
		return world.getLocalToParentTransform().createConcatenation( Transform.scale( getDesignShapeScaleX(), getDesignShapeScaleY() ) );
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

	//	@Override
	//	public List<DesignShape> doFindByShape( final DesignShape selector, final boolean intersect ) {
	//		mapSelectAperture( selector );
	//		return super.doFindByShape( selector, intersect );
	//	}

	final Pane layersPane() {
		return layers;
	}

	Bounds getVisualBounds( Node node ) {
		return node.getBoundsInParent();
	}

	double getDesignShapeScaleX() {
		return designShapeScaleX.get();
	}

	private DoubleProperty shapeScaleXProperty() {
		return designShapeScaleX;
	}

	double getDesignShapeScaleY() {
		return designShapeScaleY.get();
	}

	private DoubleProperty shapeScaleYProperty() {
		return designShapeScaleY;
	}

	double getDesignUnitScale() {
		return designUnitScale.get();
	}

	void setDesignUnitScale( double designUnitScale ) {
		this.designUnitScale.set( designUnitScale );
	}

	private DoubleProperty designUnitScaleProperty() {
		return designUnitScale;
	}

	void setDesignUnit( DesignUnit unit ) {
		setDesignUnitScale( unit.to( 1, DesignUnit.IN ) );
	}

	@Note( Note.ANY_THREAD )
	void updateGridFxGeometry() {
		// Get a local reference for thread safety
		final Workplane workplane = this.workplane;

		Fx.onFxOrCurrent( () -> {
			if( workplane == null ) {
				grid.getChildren().clear();
			} else {
				workplane.getGridSystem().updateFxGeometryGrid( workplane, getDesignShapeScaleX(), grid.getChildren() );
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
		return mapDesignLayer( designLayer, new Pane(), false );
	}

	private Pane mapDesignLayer( DesignLayer designLayer, Pane pane, boolean includeSubLayers ) {
		// Link the DesignLayer and Pane references
		putFxGeometry( designLayer, pane );
		pane.setUserData( designLayer );

		// Register event handlers for the DesignLayer
		designLayer.register(
			pane, DataNodeEvent.ANY, e -> {
				if( e.getEventType() == DataNodeEvent.NODE_CHANGED ) return;
				if( e.getEventType() == DataNodeEvent.VALUE_CHANGED ) return;

				if( e.getEventType() == DataNodeEvent.CHILD_ADDED ) {
					if( e.getNewValue() instanceof DesignLayer layer ) {
						Fx.run( () -> pane.getChildren().add( mapDesignLayer( layer ) ) );
					} else if( e.getNewValue() instanceof DesignShape shape ) {
						Fx.run( () -> pane.getChildren().add( mapDesignShape( designLayer, shape ) ) );
					} else {
						log.atTrace().log( "Unable to add unhandled child={0}", e.getNewValue() );
					}
				} else if( e.getEventType() == DataNodeEvent.CHILD_REMOVED ) {
					if( e.getOldValue() instanceof DesignLayer layer ) {
						Fx.run( () -> pane.getChildren().remove( getFxGeometry( layer ) ) );
					} else if( e.getOldValue() instanceof DesignShape shape ) {
						Fx.run( () -> pane.getChildren().remove( getFxGeometry( shape ) ) );
					} else {
						log.atTrace().log( "Unable to remove unhandled child={0}", e.getOldValue() );
					}
				}
			}
		);

		designLayer.getShapes().forEach( shape -> {
			Shape fxShape = mapDesignShape( designLayer, shape );
			Fx.run( () -> pane.getChildren().add( fxShape ) );

			// TODO Handlers need to be attached with the pane as owner
			// i.e. designLayer.register(layer, "order", e -> changeLayerOrder() );
		} );

		if( includeSubLayers ) {
			designLayer.getLayers().forEach( subLayer -> pane.getChildren().add( mapDesignLayer( subLayer ) ) );
		}

		return pane;
	}

	@SuppressWarnings( "unchecked" )
	private Shape mapSelectAperture( DesignShape aperture ) {
		Shape fxShape = getFxGeometry( aperture );

		// If an FX shape is already bound, don't do it again
		if( fxShape != null ) return fxShape;

		fxShape = switch( aperture.getType() ) {
			case BOX -> bindBoxAperture( (DesignBox)aperture );
			case ELLIPSE -> bindEllipseAperture( (DesignEllipse)aperture );
			default -> null;
		};
		if( fxShape == null ) return null;

		fxShape.setManaged( false );
		fxShape.setUserData( aperture );
		putFxGeometry( aperture, fxShape );

		return fxShape;
	}

	private Shape mapDesignShape( DesignLayer designLayer, DesignShape designShape ) {
		Shape fxShape = getFxGeometry( designShape );

		// If an FX shape is already bound, don't do it again
		if( fxShape != null ) return fxShape;

		fxShape = switch( designShape.getType() ) {
			case ARC -> bindArcGeometry( designLayer, (DesignArc)designShape );
			case BOX -> bindBoxGeometry( designLayer, (DesignBox)designShape );
			case CUBIC -> bindCubicGeometry( designLayer, (DesignCubic)designShape );
			case ELLIPSE -> bindEllipseGeometry( designLayer, (DesignEllipse)designShape );
			case LINE -> bindLineGeometry( designLayer, (DesignLine)designShape );
			case MARKER -> bindMarkerGeometry( designLayer, (DesignMarker)designShape );
			case PATH -> bindPathGeometry( designLayer, (DesignPath)designShape );
			case QUAD -> bindQuadGeometry( designLayer, (DesignQuad)designShape );
			case TEXT -> bindTextGeometry( designLayer, (DesignText)designShape );
		};

		fxShape.setManaged( false );
		fxShape.setUserData( designShape );
		putFxGeometry( designShape, fxShape );

		return fxShape;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T extends Node> T getFxGeometry( DesignDrawable drawable ) {
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

	private Arc bindArcGeometry( DesignLayer designLayer, DesignArc designArc ) {
		Arc arc = new Arc();

		bindCommonShapeGeometry( designLayer, designArc, arc );

		DesignDoubleBinding originXValue = new DesignDoubleBinding( designArc, DesignArc.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designArc, DesignArc.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding radiusXValue = new DesignDoubleBinding( designArc, DesignArc.RADII, v -> v.getRadii() != null ? v.getRadii().getX() : 0.0 );
		DesignDoubleBinding radiusYValue = new DesignDoubleBinding( designArc, DesignArc.RADII, v -> v.getRadii() != null ? v.getRadii().getY() : 0.0 );
		DesignDoubleBinding startAngleValue = new DesignDoubleBinding( designArc, DesignArc.START, DesignArc::calcStart );
		DesignDoubleBinding lengthValue = new DesignDoubleBinding( designArc, DesignArc.EXTENT, DesignArc::calcExtent );
		DesignDoubleBinding rotateValue = new DesignDoubleBinding( designArc, DesignArc.ROTATE, DesignArc::calcRotate );

		arc.centerXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		arc.centerYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		arc.radiusXProperty().bind( shapeScaleXProperty().multiply( radiusXValue ) );
		arc.radiusYProperty().bind( shapeScaleYProperty().multiply( radiusYValue ) );
		arc.startAngleProperty().bind( startAngleValue.negate() );
		arc.lengthProperty().bind( lengthValue.negate() );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateValue );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		arc.getTransforms().setAll( rotate );

		return arc;
	}

	private Rectangle bindBoxGeometry( DesignLayer designLayer, DesignBox designBox ) {
		Rectangle box = new Rectangle();

		bindCommonShapeGeometry( designLayer, designBox, box );

		DesignDoubleBinding originXValue = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding widthValue = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize() != null ? v.getSize().getX() : 0.0 );
		DesignDoubleBinding heightValue = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize() != null ? v.getSize().getY() : 0.0 );
		DesignDoubleBinding rotateValue = new DesignDoubleBinding( designBox, DesignBox.ROTATE, DesignBox::calcRotate );

		// Box supports negative width and height
		NumberBinding xValue = Bindings.min( originXValue, originXValue.add( widthValue ) );
		NumberBinding yValue = Bindings.min( originYValue, originYValue.add( heightValue ) );
		DoubleProperty wValue = new SimpleDoubleProperty();
		DoubleProperty hValue = new SimpleDoubleProperty();
		wValue.bind( widthValue.map( d -> Math.abs( (double)d ) ) );
		hValue.bind( heightValue.map( d -> Math.abs( (double)d ) ) );

		box.xProperty().bind( shapeScaleXProperty().multiply( xValue ) );
		box.yProperty().bind( shapeScaleYProperty().multiply( yValue ) );
		box.widthProperty().bind( shapeScaleXProperty().multiply( wValue ) );
		box.heightProperty().bind( shapeScaleYProperty().multiply( hValue ) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateValue );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		box.getTransforms().setAll( rotate );

		return box;
	}

	private CubicCurve bindCubicGeometry( DesignLayer designLayer, DesignCubic designCubic ) {
		CubicCurve quad = new CubicCurve();

		bindCommonShapeGeometry( designLayer, designCubic, quad );

		DesignDoubleBinding startXValue = new DesignDoubleBinding( designCubic, DesignCubic.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding startYValue = new DesignDoubleBinding( designCubic, DesignCubic.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding originControlXValue = new DesignDoubleBinding( designCubic, DesignCubic.ORIGIN_CONTROL, v -> v.getOriginControl() != null ? v.getOriginControl().getX() : 0.0 );
		DesignDoubleBinding originControlYValue = new DesignDoubleBinding( designCubic, DesignCubic.ORIGIN_CONTROL, v -> v.getOriginControl() != null ? v.getOriginControl().getY() : 0.0 );
		DesignDoubleBinding pointControlXValue = new DesignDoubleBinding( designCubic, DesignCubic.POINT_CONTROL, v -> v.getPointControl() != null ? v.getPointControl().getX() : 0.0 );
		DesignDoubleBinding pointControlYValue = new DesignDoubleBinding( designCubic, DesignCubic.POINT_CONTROL, v -> v.getPointControl() != null ? v.getPointControl().getY() : 0.0 );
		DesignDoubleBinding pointXValue = new DesignDoubleBinding( designCubic, DesignCubic.POINT, v -> v.getPoint() != null ? v.getPoint().getX() : 0.0 );
		DesignDoubleBinding pointYValue = new DesignDoubleBinding( designCubic, DesignCubic.POINT, v -> v.getPoint() != null ? v.getPoint().getY() : 0.0 );

		quad.startXProperty().bind( shapeScaleXProperty().multiply( startXValue ) );
		quad.startYProperty().bind( shapeScaleYProperty().multiply( startYValue ) );
		quad.controlX1Property().bind( shapeScaleXProperty().multiply( originControlXValue ) );
		quad.controlY1Property().bind( shapeScaleYProperty().multiply( originControlYValue ) );
		quad.controlX2Property().bind( shapeScaleXProperty().multiply( pointControlXValue ) );
		quad.controlY2Property().bind( shapeScaleYProperty().multiply( pointControlYValue ) );
		quad.endXProperty().bind( shapeScaleXProperty().multiply( pointXValue ) );
		quad.endYProperty().bind( shapeScaleYProperty().multiply( pointYValue ) );

		return quad;
	}

	private Ellipse bindEllipseGeometry( DesignLayer designLayer, DesignEllipse designEllipse ) {
		Ellipse ellipse = new Ellipse();

		bindCommonShapeGeometry( designLayer, designEllipse, ellipse );

		DesignDoubleBinding originXValue = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding radiusXValue = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii() != null ? v.getRadii().getX() : 0.0 );
		DesignDoubleBinding radiusYValue = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii() != null ? v.getRadii().getY() : 0.0 );
		DesignDoubleBinding rotateValue = new DesignDoubleBinding( designEllipse, DesignEllipse.ROTATE, DesignEllipse::calcRotate );

		ellipse.centerXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		ellipse.centerYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		ellipse.radiusXProperty().bind( shapeScaleXProperty().multiply( radiusXValue ) );
		ellipse.radiusYProperty().bind( shapeScaleYProperty().multiply( radiusYValue ) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateValue );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		ellipse.getTransforms().setAll( rotate );

		return ellipse;
	}

	private Line bindLineGeometry( DesignLayer designLayer, DesignLine designLine ) {
		Line line = new Line();

		bindCommonShapeGeometry( designLayer, designLine, line );

		DesignDoubleBinding startXValue = new DesignDoubleBinding( designLine, DesignLine.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding startYValue = new DesignDoubleBinding( designLine, DesignLine.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding pointXValue = new DesignDoubleBinding( designLine, DesignLine.POINT, v -> v.getPoint() != null ? v.getPoint().getX() : 0.0 );
		DesignDoubleBinding pointYValue = new DesignDoubleBinding( designLine, DesignLine.POINT, v -> v.getPoint() != null ? v.getPoint().getY() : 0.0 );

		line.startXProperty().bind( shapeScaleXProperty().multiply( startXValue ) );
		line.startYProperty().bind( shapeScaleYProperty().multiply( startYValue ) );
		line.endXProperty().bind( shapeScaleXProperty().multiply( pointXValue ) );
		line.endYProperty().bind( shapeScaleYProperty().multiply( pointYValue ) );

		return line;
	}

	private Path bindMarkerGeometry( DesignLayer designLayer, DesignMarker designMarker ) {
		Path path = new Path();

		bindCommonShapeGeometry( designLayer, designMarker, path );
		path.setFillRule( FillRule.EVEN_ODD );

		// Create a DesignBinding on TYPE
		DesignStringBinding markerType = new DesignStringBinding( designMarker, DesignMarker.TYPE, DesignMarker::getMarkerType );

		// Bind on steps and update the path geometry
		ObjectBinding<List<PathElement>> elementsBinding = Bindings.createObjectBinding(
			() -> {
				double shapeScaleX = getDesignShapeScaleX();
				double shapeScaleY = getDesignShapeScaleY();
				return designMarker.getSteps().stream().map( step -> pathElementMapper.map( step, shapeScaleX, shapeScaleY )).toList();
			}, markerType, shapeScaleXProperty(), shapeScaleYProperty()
		);
		path.getElements().setAll( elementsBinding.get() );
		elementsBinding.addListener( ( _, _, n ) -> path.getElements().setAll( n ) );

		return path;
	}

	private Path bindPathGeometry( DesignLayer designLayer, DesignPath designPath ) {
		Path path = new Path();

		bindCommonShapeGeometry( designLayer, designPath, path );

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
			}, stepsBinding, shapeScaleXProperty(), shapeScaleYProperty()
		);
		path.getElements().setAll( elementsBinding.get() );
		elementsBinding.addListener( ( _, _, n ) -> path.getElements().setAll( n ) );

		return path;
	}

	private QuadCurve bindQuadGeometry( DesignLayer designLayer, DesignQuad designQuad ) {
		QuadCurve quad = new QuadCurve();

		bindCommonShapeGeometry( designLayer, designQuad, quad );

		DesignDoubleBinding startXValue = new DesignDoubleBinding( designQuad, DesignQuad.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding startYValue = new DesignDoubleBinding( designQuad, DesignQuad.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding controlXValue = new DesignDoubleBinding( designQuad, DesignQuad.CONTROL, v -> v.getControl() != null ? v.getControl().getX() : 0.0 );
		DesignDoubleBinding controlYValue = new DesignDoubleBinding( designQuad, DesignQuad.CONTROL, v -> v.getControl() != null ? v.getControl().getY() : 0.0 );
		DesignDoubleBinding pointXValue = new DesignDoubleBinding( designQuad, DesignQuad.POINT, v -> v.getPoint() != null ? v.getPoint().getX() : 0.0 );
		DesignDoubleBinding pointYValue = new DesignDoubleBinding( designQuad, DesignQuad.POINT, v -> v.getPoint() != null ? v.getPoint().getY() : 0.0 );

		quad.startXProperty().bind( shapeScaleXProperty().multiply( startXValue ) );
		quad.startYProperty().bind( shapeScaleYProperty().multiply( startYValue ) );
		quad.controlXProperty().bind( shapeScaleXProperty().multiply( controlXValue ) );
		quad.controlYProperty().bind( shapeScaleYProperty().multiply( controlYValue ) );
		quad.endXProperty().bind( shapeScaleXProperty().multiply( pointXValue ) );
		quad.endYProperty().bind( shapeScaleYProperty().multiply( pointYValue ) );

		return quad;
	}

	// Eventually this should only have to be called once per design shape
	private Text bindTextGeometry( DesignLayer designLayer, DesignText designText ) {
		Text text = new Text();

		bindCommonShapeGeometry( designLayer, designText, text );

		DesignDoubleBinding originXValue = new DesignDoubleBinding( designText, DesignText.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designText, DesignText.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding rotateValue = new DesignDoubleBinding( designText, DesignText.ROTATE, DesignShape::calcRotate );
		DesignBinding<String> textValue = new DesignBinding<>( designText, DesignText.TEXT, DesignText::getText );
		DesignBinding<String> fontNameValue = new DesignBinding<>( designText, DesignText.FONT_NAME, DesignText::getFontName );
		DesignBinding<FontWeight> fontWeightValue = new DesignBinding<>( designText, DesignText.FONT_WEIGHT, DesignText::calcFontWeight );
		DesignBinding<FontPosture> fontPostureValue = new DesignBinding<>( designText, DesignText.FONT_POSTURE, DesignText::calcFontPosture );
		DesignDoubleBinding textSizeValue = new DesignDoubleBinding( designText, DesignText.TEXT_SIZE, DesignText::calcTextSize );

		DesignBinding<String> layerFontNameValue = new DesignBinding<>( designLayer, DesignText.FONT_NAME, DesignLayer::getFontName );
		DesignBinding<FontWeight> layerFontWeightValue = new DesignBinding<>( designLayer, DesignText.FONT_WEIGHT, DesignLayer::calcFontWeight );
		DesignBinding<FontPosture> layerFontPostureValue = new DesignBinding<>( designLayer, DesignText.FONT_POSTURE, DesignLayer::calcFontPosture );
		DesignDoubleBinding layerTextSizeValue = new DesignDoubleBinding( designLayer, DesignText.TEXT_SIZE, DesignLayer::calcTextSize );

		text.textProperty().bind( textValue );
		text.xProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		text.yProperty().bind( shapeScaleYProperty().multiply( originYValue ).negate() );
		text.fontProperty().bind( Bindings.createObjectBinding(
			() -> Font.font( designText.calcFontName(), designText.calcFontWeight(), designText.calcFontPosture(), designText.calcTextSize() * shapeScaleYProperty().get() ),
			fontNameValue,
			fontWeightValue,
			fontPostureValue,
			textSizeValue,
			layerFontNameValue,
			layerFontWeightValue,
			layerFontPostureValue,
			layerTextSizeValue,
			shapeScaleYProperty()
		) );

		Rotate rotate = new Rotate();
		rotate.angleProperty().bind( rotateValue );
		rotate.pivotXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		rotate.pivotYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );

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
	private void bindCommonShapeGeometry( DesignLayer designLayer, DesignShape designShape, Shape shape ) {
		DesignBooleanBinding isSelected = new DesignBooleanBinding( designShape, DesignShape.SELECTED, DesignShape::isSelected );
		DesignBinding<Paint> shapeFill = new DesignBinding<>( designShape, DesignShape.FILL_PAINT, DesignShape::calcFillPaint );
		DesignBinding<Paint> shapeDraw = new DesignBinding<>( designShape, DesignShape.DRAW_PAINT, DesignShape::calcDrawPaint );
		DesignDoubleBinding shapeDrawWidth = new DesignDoubleBinding( designShape, DesignShape.DRAW_WIDTH, DesignShape::calcDrawWidth );
		DesignBinding<StrokeLineCap> shapeDrawCap = new DesignBinding<>( designShape, DesignShape.DRAW_CAP, DesignShape::calcDrawCap );
		DesignBinding<StrokeLineJoin> shapeDrawJoin = new DesignBinding<>( designShape, DesignShape.DRAW_JOIN, DesignShape::calcDrawJoin );
		DesignDoubleBinding shapeDashOffset = new DesignDoubleBinding( designShape, DesignShape.DASH_OFFSET, DesignShape::calcDashOffset );
		DesignBinding<List<Double>> shapePatternBinding = new DesignBinding<>( designShape, DesignShape.DASH_PATTERN, DesignShape::calcDashPattern );

		DesignBinding<Paint> layerFill = new DesignBinding<>( designLayer, DesignLayer.FILL_PAINT, DesignLayer::calcFillPaint );
		DesignBinding<Paint> layerDraw = new DesignBinding<>( designLayer, DesignLayer.DRAW_PAINT, DesignLayer::calcDrawPaint );
		DesignDoubleBinding layerDrawWidth = new DesignDoubleBinding( designLayer, DesignLayer.DRAW_WIDTH, DesignLayer::calcDrawWidth );
		DesignBinding<StrokeLineCap> layerDrawCap = new DesignBinding<>( designLayer, DesignLayer.DRAW_CAP, DesignLayer::calcDrawCap );
		DesignBinding<StrokeLineJoin> layerDrawJoin = new DesignBinding<>( designLayer, DesignLayer.DRAW_JOIN, DesignLayer::calcDrawJoin );
		DesignDoubleBinding layerDashOffset = new DesignDoubleBinding( designLayer, DesignLayer.DASH_OFFSET, DesignLayer::calcDashOffset );
		DesignBinding<List<Double>> layerPatternBinding = new DesignBinding<>( designLayer, DesignLayer.DASH_PATTERN, DesignLayer::calcDashPattern );

		BooleanBinding hasFill = Bindings.and( shapeFill.isNotNull(), shapeFill.isNotEqualTo( Color.TRANSPARENT ) );
		BooleanBinding hasDraw = Bindings.and( shapeDraw.isNotNull(), shapeDraw.isNotEqualTo( Color.TRANSPARENT ) );

		shape.fillProperty().bind( Bindings.createObjectBinding(
			() -> {
				Paint fillPaint = designShape.calcFillPaint();
				if( fillPaint != null && fillPaint != Color.TRANSPARENT ) {
					if( designShape.isSelected() ) {
						return getSelectedFillPaint();
					} else {
						return fillPaint;
					}
				}
				return null;
			}, hasFill, isSelected, shapeFill, layerFill, selectedFillPaint()
		) );
		shape.strokeProperty().bind( Bindings.createObjectBinding(
			() -> {
				Paint drawPaint = designShape.calcDrawPaint();
				if( drawPaint != null && drawPaint != Color.TRANSPARENT ) {
					if( designShape.isSelected() ) {
						return getSelectedDrawPaint();
					} else {
						return drawPaint;
					}
				}
				return null;
			}, hasDraw, isSelected, shapeDraw, layerDraw, selectedDrawPaint()
		) );
		shape.strokeWidthProperty().bind( Bindings.createObjectBinding(
			() -> {
				return getDesignShapeScaleX() * designShape.calcDrawWidth();
			}, shapeScaleXProperty(), shapeDrawWidth, layerDrawWidth
		) );
		shape.strokeLineCapProperty().bind( Bindings.createObjectBinding(
			() -> {
				return designShape.calcDrawCap();
			}, shapeDrawCap, layerDrawCap
		) );
		shape.strokeLineJoinProperty().bind( Bindings.createObjectBinding(
			() -> {
				return designShape.calcDrawJoin();
			}, shapeDrawJoin, layerDrawJoin
		) );
		// NOTE Future feature
		//shape.strokeTypeProperty().bind( Bindings.createObjectBinding(
		//	() -> {
		//		return designShape.calcDrawType();
		//	}, shapeDrawType, layerDrawType
		//));
		// NOTE Future feature
		//shape.strokeMiterLimitProperty().bind( Bindings.createObjectBinding(
		//	() -> {
		//		return designShape.calcDrawMiterLimit();
		//	}, shapeDrawMiterLimit, layerDrawMiterLimit
		//));

		// Dash offset
		shape.strokeDashOffsetProperty().bind( Bindings.createObjectBinding(
			() -> {
				return getDesignShapeScaleX() * designShape.calcDashOffset();
			}, shapeScaleXProperty(), shapeDashOffset, layerDashOffset
		) );

		// Dash pattern
		ObjectBinding<List<Double>> dashBinding = Bindings.createObjectBinding(
			() -> designShape.calcDashPattern().stream().map( d -> d * getDesignShapeScaleX() ).toList(),
			shapeScaleXProperty(),
			shapePatternBinding,
			layerPatternBinding
		);
		shape.getStrokeDashArray().setAll( dashBinding.get() );
		dashBinding.addListener( ( _, _, n ) -> shape.getStrokeDashArray().setAll( n ) );
	}

	Ellipse bindEllipseAperture( DesignEllipse designEllipse ) {
		Ellipse ellipse = new Ellipse();

		bindCommonApertureGeometry( designEllipse, ellipse );

		DesignBooleanBinding apertureVisible = new DesignBooleanBinding( designEllipse, DesignShape.VISIBLE, DesignShape::isVisible );
		DesignDoubleBinding originXValue = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designEllipse, DesignEllipse.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding radiusXValue = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii() != null ? v.getRadii().getX() : 0.0 );
		DesignDoubleBinding radiusYValue = new DesignDoubleBinding( designEllipse, DesignEllipse.RADII, v -> v.getRadii() != null ? v.getRadii().getY() : 0.0 );

		ellipse.visibleProperty().bind( Bindings.and( hotspotVisible(), apertureVisible ) );
		ellipse.centerXProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		ellipse.centerYProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		ellipse.radiusXProperty().bind( apertureShapeScaleX.multiply( radiusXValue ).divide( viewZoomXProperty() ) );
		ellipse.radiusYProperty().bind( apertureShapeScaleY.multiply( radiusYValue ).divide( viewZoomYProperty() ) );

		return ellipse;
	}

	Rectangle bindBoxAperture( DesignBox designBox ) {
		Rectangle box = new Rectangle();

		bindCommonApertureGeometry( designBox, box );

		DesignDoubleBinding originXValue = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getX() : 0.0 );
		DesignDoubleBinding originYValue = new DesignDoubleBinding( designBox, DesignBox.ORIGIN, v -> v.getOrigin() != null ? v.getOrigin().getY() : 0.0 );
		DesignDoubleBinding widthValue = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize() != null ? v.getSize().getX() : 0.0 );
		DesignDoubleBinding heightValue = new DesignDoubleBinding( designBox, DesignBox.SIZE, v -> v.getSize() != null ? v.getSize().getY() : 0.0 );

		box.visibleProperty().bind( new DesignBinding<>( designBox, DesignShape.VISIBLE, DesignShape::isVisible ) );
		box.xProperty().bind( shapeScaleXProperty().multiply( originXValue ) );
		box.yProperty().bind( shapeScaleYProperty().multiply( originYValue ) );
		box.widthProperty().bind( shapeScaleXProperty().multiply( widthValue ) );
		box.heightProperty().bind( shapeScaleYProperty().multiply( heightValue ) );

		return box;
	}

	/**
	 * Bind the common geometry properties of the shape. This method is used to
	 * bind the common shape properties to their dependent properties, whether
	 * they be FX properties or design properties.
	 *
	 * @param designShape The source design shape
	 * @param shape The target FX shape
	 */
	private void bindCommonApertureGeometry( DesignShape designShape, Shape shape ) {
		DesignDoubleBinding strokeWidthValue = new DesignDoubleBinding( designShape, DesignShape.DRAW_WIDTH, DesignShape::calcDrawWidth );

		shape.fillProperty().bind( new DesignBinding<>( designShape, DesignShape.FILL_PAINT, DesignShape::calcFillPaint ) );
		shape.setStrokeType( StrokeType.INSIDE );
		shape.strokeProperty().bind( new DesignBinding<>( designShape, DesignShape.DRAW_PAINT, DesignShape::calcDrawPaint ) );
		shape.strokeWidthProperty().bind( apertureShapeScaleX.multiply( strokeWidthValue ).divide( viewZoomXProperty() ).multiply( outputScaleXProperty() ) );
	}

	private record GeometryKey(DesignRenderer renderer, DesignDrawable drawable) {}

}
