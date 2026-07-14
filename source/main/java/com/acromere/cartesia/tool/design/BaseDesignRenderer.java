package com.acromere.cartesia.tool.design;

import com.acromere.annotation.Note;
import com.acromere.cartesia.CartesiaNote;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.DesignValue;
import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignEllipse;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.tool.RenderConstants;
import com.acromere.zerra.color.Colors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import lombok.CustomLog;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public abstract class BaseDesignRenderer extends StackPane implements DesignRenderer, RenderConstants {

	private final DoubleProperty dpiX;

	private final DoubleProperty dpiY;

	private final DoubleProperty outputScaleX;

	private final DoubleProperty outputScaleY;

	private final DoubleProperty viewCenterX;

	private final DoubleProperty viewCenterY;

	private final DoubleProperty viewCenterZ;

	private final DoubleProperty viewRotate;

	private final DoubleProperty viewZoomX;

	private final DoubleProperty viewZoomY;

	private final ObservableList<DesignLayer> enabledLayers;

	private final ObservableList<DesignLayer> visibleLayers;

	private final SimpleObjectProperty<DesignShape> selectAperture;

	private final SimpleStringProperty apertureDrawPaint;

	private final SimpleStringProperty apertureFillPaint;

	public BaseDesignRenderer() {
		getStyleClass().add( "tool-renderer" );

		/*
	  The renderer is configured to render at 96 DPI by default, but it can be
		configured to render for different media just as easily by changing the
		DPI setting:

		screen: setDpi( Screen.getPrimary().getDpi() );
		printer: setDpi( PrintResolution.getCrossFeedResolution(), PrintResolution.getFeedResolution() );
		*/
		dpiX = new SimpleDoubleProperty( DEFAULT_DPI );
		dpiY = new SimpleDoubleProperty( DEFAULT_DPI );

		/*
		The output scale is used when working with high resolution (HiDPI) monitors.
		This allows for fractional scaling as well and needs to be taken into account
		for detailed rendering of the model when the application may not require it.
		 */
		outputScaleX = new SimpleDoubleProperty( DEFAULT_OUTPUT_SCALE );
		outputScaleY = new SimpleDoubleProperty( DEFAULT_OUTPUT_SCALE );

		/*
		The world view settings. These are the view settings from the user's
		perspective.
		 */
		viewCenterX = new SimpleDoubleProperty( DEFAULT_CENTER.getX() );
		viewCenterY = new SimpleDoubleProperty( DEFAULT_CENTER.getY() );
		viewCenterZ = new SimpleDoubleProperty( DEFAULT_CENTER.getZ() );
		viewRotate = new SimpleDoubleProperty( DEFAULT_ROTATE );
		viewZoomX = new SimpleDoubleProperty( DEFAULT_ZOOM );
		viewZoomY = new SimpleDoubleProperty( DEFAULT_ZOOM );

		enabledLayers = FXCollections.observableArrayList();
		visibleLayers = FXCollections.observableArrayList();

		selectAperture = new SimpleObjectProperty<>( DEFAULT_SELECT_APERTURE );
		apertureDrawPaint = new SimpleStringProperty( Colors.toString( Colors.translucent( Color.YELLOW, 0.8 ) ) );
		apertureFillPaint = new SimpleStringProperty( Colors.toString( Colors.translucent( Color.YELLOW, 0.2 ) ) );

	}

	/**
	 * Set the DPI for the renderer. This method sets the DPI for both the X and Y
	 * axes.
	 *
	 * @param dpiX The DPI to set for the X axis
	 * @param dpiY The DPI to set for the Y axis
	 */
	@Override
	public void setDpi( double dpiX, double dpiY ) {
		setDpiX( dpiX );
		setDpiY( dpiY );
	}

	@Override
	public void setDpiX( double dpi ) {
		dpiX.set( dpi );
	}

	@Override
	public double getDpiX() {
		return dpiX.get();
	}

	@Override
	public DoubleProperty dpiXProperty() {
		return dpiX;
	}

	@Override
	public double getDpiY() {
		return dpiY.get();
	}

	@Override
	public void setDpiY( double dpi ) {
		dpiY.set( dpi );
	}

	@Override
	public DoubleProperty dpiYProperty() {
		return dpiY;
	}

	@Override
	public void setOutputScale( double scaleX, double scaleY ) {
		setOutputScaleX( scaleX );
		setOutputScaleY( scaleY );
	}

	@Override
	public double getOutputScaleX() {
		return outputScaleX.get();
	}

	@Override
	public void setOutputScaleX( double scale ) {
		outputScaleX.set( scale );
	}

	@Override
	public DoubleProperty outputScaleXProperty() {
		return outputScaleX;
	}

	@Override
	public double getOutputScaleY() {
		return outputScaleY.get();
	}

	@Override
	public void setOutputScaleY( double scale ) {
		outputScaleY.set( scale );
	}

	@Override
	public DoubleProperty outputScaleYProperty() {
		return outputScaleY;
	}

	@Override
	public Point3D getViewCenter() {
		return new Point3D( viewCenterX.get(), viewCenterY.get(), viewCenterZ.get() );
	}

	public void setViewCenter( double x, double y ) {
		setViewCenter( x, y, 0 );
	}

	@Override
	public void setViewCenter( double x, double y, double z ) {
		viewCenterX.set( x );
		viewCenterY.set( y );
		viewCenterZ.set( z );
	}

	@Override
	public void setViewCenter( Point3D center ) {
		setViewCenter( center.getX(), center.getY(), center.getZ() );
	}

	@Override
	public double getViewCenterX() {
		return viewCenterX.get();
	}

	@Override
	public void setViewCenterX( double x ) {
		viewCenterX.set( x );
	}

	@Override
	public DoubleProperty viewCenterXProperty() {
		return viewCenterX;
	}

	@Override
	public double getViewCenterY() {
		return viewCenterY.get();
	}

	@Override
	public void setViewCenterY( double y ) {
		viewCenterY.set( y );
	}

	@Override
	public DoubleProperty viewCenterYProperty() {
		return viewCenterY;
	}

	@Override
	public double getViewCenterZ() {
		return viewCenterZ.get();
	}

	@Override
	public void setViewCenterZ( double z ) {
		viewCenterZ.set( z );
	}

	@Override
	public DoubleProperty viewCenterZProperty() {
		return viewCenterZ;
	}

	@Override
	public double getViewRotate() {
		return viewRotate.get();
	}

	@Override
	public void setViewRotate( double rotate ) {
		viewRotate.set( rotate );
	}

	@Override
	public DoubleProperty viewRotateProperty() {
		return viewRotate;
	}

	@Override
	public double getViewZoom() {
		return getViewZoomX();
	}

	@Override
	public void setViewZoom( double viewZoom ) {
		setViewZoom( viewZoom, viewZoom );
	}

	@Override
	public void setViewZoom( double zoomX, double zoomY ) {
		setViewZoomX( zoomX );
		setViewZoomY( zoomY );
	}

	@Override
	public void setViewZoom( Point2D zoom ) {
		setViewZoomX( zoom.getX() );
		setViewZoomY( zoom.getY() );
	}

	@Override
	public double getViewZoomX() {
		return viewZoomX.get();
	}

	@Override
	public void setViewZoomX( double zoom ) {
		viewZoomX.set( zoom );
	}

	@Override
	public DoubleProperty viewZoomXProperty() {
		return viewZoomX;
	}

	@Override
	public double getViewZoomY() {
		return viewZoomY.get();
	}

	@Override
	public void setViewZoomY( double zoom ) {
		viewZoomY.set( zoom );
	}

	@Override
	public DoubleProperty viewZoomYProperty() {
		return viewZoomY;
	}

	/**
	 * Change the current zoom by the zoom factor. The zoom is centered on the
	 * provided anchor point in world coordinates. The current zoom is changed by
	 * multiplying the current zoom by the factor, and that becomes the new zoom.
	 *
	 * @param anchor The anchor point in world coordinates
	 * @param factor The zoom factor
	 */
	public void zoom( Point3D anchor, double factor ) {
		Point3D offset = getViewCenter().subtract( anchor );

		// The new view zoom has to be set before the new view center
		setViewZoom( new Point2D( viewZoomX.get(), viewZoomY.get() ).multiply( factor ) );

		// The new view center has to be set after the new view zoom
		setViewCenter( anchor.add( offset.multiply( 1.0 / factor ) ) );
	}

	@Override
	public boolean isLayerEnabled( DesignLayer layer ) {
		return enabledLayers.contains( layer );
	}

	@Override
	public void setLayerEnabled( DesignLayer layer, boolean enabled ) {
		if( enabled ) {
			enabledLayers().add( layer );
		} else {
			enabledLayers().remove( layer );
		}
	}

	@Override
	@Note( CartesiaNote.IN_DESIGN_TOOL_NEXT )
	public List<DesignLayer> getEnabledLayers() {
		return List.copyOf( enabledLayers );
	}

	@Override
	public void setEnabledLayers( Collection<DesignLayer> layers ) {
		enabledLayers().setAll( layers );
	}

	@Override
	public ObservableList<DesignLayer> enabledLayers() {
		return enabledLayers;
	}

	@Override
	public boolean isLayerVisible( DesignLayer layer ) {
		return visibleLayers.contains( layer );
	}

	@Override
	public void setLayerVisible( DesignLayer layer, boolean visible ) {
		if( visible ) {
			visibleLayers().add( layer );
		} else {
			visibleLayers().remove( layer );
		}
	}

	@Override
	public List<DesignLayer> getVisibleLayers() {
		return List.copyOf( visibleLayers );
	}

	@Override
	public void setVisibleLayers( Collection<DesignLayer> layers ) {
		visibleLayers().setAll( layers );
	}

	@Override
	public ObservableList<DesignLayer> visibleLayers() {
		return visibleLayers;
	}

	// Visible Shapes ------------------------------------------------------------

	public List<DesignShape> getVisibleShapes() {
		return getVisibleLayers().stream().flatMap( l -> l.getShapes().stream() ).collect( Collectors.toList() );
	}

	// Selecting -----------------------------------------------------------------

	@Override
	public void setSelectAperture( DesignShape aperture ) {
//		if( aperture == null ) aperture = DEFAULT_SELECT_APERTURE;
//
//		// The selector shape is defined in world coordinates
//		if( aperture != DEFAULT_SELECT_APERTURE ) {
//			if( aperture instanceof DesignEllipse ) {
//				aperture.setDrawPaint( "#00000000" );
//			} else if( aperture instanceof DesignBox ) {
//				aperture.setDrawPaint( getApertureDrawPaint() );
//			}
//			aperture.setFillPaint( getApertureFillPaint() );
//		}

		if( aperture == null ) {
			selectAperture.set( DEFAULT_SELECT_APERTURE );
		} else {
			aperture.setDrawPaint( getApertureDrawPaint() );
			aperture.setFillPaint( getApertureFillPaint() );
			selectAperture.set( aperture );
		}
	}

	public DesignShape getSelectAperture() {
		return selectAperture.get();
	}

	public SimpleObjectProperty<DesignShape> selectAperture() {
		return selectAperture;
	}

	public Paint calcApertureDrawPaint() {
		// TODO Cache this value for rendering performance
		return Colors.parse( getApertureDrawPaint() );
	}

	public String getApertureDrawPaint() {
		return apertureDrawPaint.get();
	}

	public void setApertureDrawPaint( String paint ) {
		apertureDrawPaint.set( paint );
		if( selectAperture.get() != null && selectAperture.get() != DEFAULT_SELECT_APERTURE ) selectAperture.get().setDrawPaint( paint );
	}

	public SimpleStringProperty apertureDrawPaint() {
		return apertureDrawPaint;
	}

	public Paint calcApertureFillPaint() {
		// TODO Cache this value for rendering performance
		return Colors.parse( getApertureFillPaint() );
	}

	public String getApertureFillPaint() {
		return apertureFillPaint.get();
	}

	public void setApertureFillPaint( String paint ) {
		apertureFillPaint.set( paint );
		if( selectAperture.get() != null && selectAperture.get() != DEFAULT_SELECT_APERTURE ) selectAperture.get().setFillPaint( paint );
	}

	public SimpleStringProperty apertureFillPaint() {
		return apertureFillPaint;
	}

	/**
	 * Find the nodes contained by, or intersecting, the window specified by points a and b.
	 *
	 * @param a One corner of the window
	 * @param b The other corner of the window
	 * @param intersect True to select shapes by intersection
	 * @return The set of discovered nodes
	 */
	public List<DesignShape> worldWindowFind( Point3D a, Point3D b, boolean intersect ) {
		double x = Math.min( a.getX(), b.getX() );
		double y = Math.min( a.getY(), b.getY() );
		double w = Math.abs( a.getX() - b.getX() );
		double h = Math.abs( a.getY() - b.getY() );

		DesignBox selector = new DesignBox( x, y, w, h );
		return doFindByShape( selector, intersect );
	}

	public List<DesignShape> worldPointFind( Point3D anchor, DesignValue tolerance ) {
		double radius = realToWorld( tolerance );
		Point3D radii = new Point3D( radius, radius, 0 );

		DesignEllipse selector = new DesignEllipse( anchor, radii );
		return doFindByShape( selector, true );
	}

	/**
	 * Convert the provided value to world units
	 *
	 * @param value The value to convert
	 * @return The value in world units
	 */
	double realToWorld( DesignValue value ) {
		// Convert the provided value to design units and divide by the zoom factor
		return value.to( getDesign().getDataModel().calcDesignUnit() ).value() / getViewZoomX();
	}

	/**
	 * Convert the provided value to screen units
	 *
	 * @param value The value to convert
	 * @return The value in screen units
	 */
	double realToScreen( DesignValue value ) {
		// Convert the provided value to inches and multiply by DPI
		return value.to( DesignUnit.IN ).value() * getDpiX();
	}

	/**
	 * Select nodes using a shape. The selecting shape can be any shape but it
	 * usually a {@link DesignEllipse} or a {@link DesignBox}. Returns the list
	 * of discovered shapes in order from top to bottom.
	 * <p>
	 * The selector shape is defined in world coordinates.
	 *
	 * @param selector The selecting shape
	 * @param intersect True to select shapes by intersection
	 * @return The list of discovered shapes
	 */
	protected List<DesignShape> doFindByShape( final DesignShape selector, final boolean intersect ) {
		// Ensure the selector does not have a draw width
		selector.setDrawWidth( "0" );
		selector.setDrawPaint( "#ff00ffff" );
		selector.setFillPaint( "#ff00ffff" );

		// This method should be thread agnostic. It should be safe to call from any thread.
		return getVisibleShapes().stream().filter( shape -> matches( selector, shape, intersect ) ).collect( Collectors.toList() );
	}

	/**
	 * Test if the selector shape should select the specific shape. The intersect
	 * parameter indicates if the selector needs to contain or just intersect the
	 * shape.
	 * <p>
	 * Both the selector and the shape are defined in world coordinates.
	 *
	 * @param selector The selector shape
	 * @param shape The shape to test
	 * @param intersect The intersect flag
	 * @return True if the selector shape should select the shape
	 */
	private boolean matches( DesignShape selector, DesignShape shape, boolean intersect ) {
		Bounds selectorBounds = selector.getSelectBounds();
		Bounds shapeBounds = shape.getSelectBounds();

		// This first test is an optimization for fully excluded shapes
		if( !selectorBounds.intersects( shapeBounds ) ) return false;

		// This second test is an optimization for fully contained shapes
		if( selectorBounds.contains( shapeBounds ) ) return true;

		// This is the slow but accurate test if the shape is matched when the selector is not a box
		Shape fxSelector = selector.getFxShape();
		Shape fxShape = shape.getFxShape();

		// Check if the selector should match the shape
		if( intersect ) {
			return !((javafx.scene.shape.Path)Shape.intersect( fxShape, fxSelector )).getElements().isEmpty();
		} else {
			return ((javafx.scene.shape.Path)Shape.subtract( fxShape, fxSelector )).getElements().isEmpty();
		}
	}

}
