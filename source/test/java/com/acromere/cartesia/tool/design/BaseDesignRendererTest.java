package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUnitTest;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.DesignValue;
import com.acromere.cartesia.data.*;
import com.acromere.cartesia.tool.RenderConstants;
import com.acromere.zerra.javafx.Fx;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acromere.cartesia.TestTimeouts.FX_STABILITY_TIMEOUT;
import static com.acromere.cartesia.tool.RenderConstants.WINDOW_SELECT_APERTURE;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseDesignRendererTest extends BaseCartesiaUnitTest {

	private final BaseDesignRenderer renderer;

	protected BaseDesignRendererTest( BaseDesignRenderer renderer ) {
		this.renderer = renderer;
	}

	protected BaseDesignRenderer getRenderer() {
		return renderer;
	}

	@Test
	void enabledLayers() {
		// given
		DesignLayer first = new DesignLayer();
		DesignLayer second = new DesignLayer();
		List<DesignLayer> layers = List.of( first, second );
		getRenderer().setEnabledLayers( layers );

		// when
		ObservableList<DesignLayer> enabledLayers = renderer.enabledLayers();

		// then
		assertThat( enabledLayers ).isEqualTo( layers );
		assertThat( getRenderer().getEnabledLayers() ).isEqualTo( layers );
		assertThat( getRenderer().isLayerEnabled( first ) ).isTrue();
		assertThat( getRenderer().isLayerEnabled( second ) ).isTrue();
	}

	@Test
	void setLayerEnabled() {
		// given
		DesignLayer layer = new DesignLayer();

		// when
		getRenderer().setLayerEnabled( layer, true );

		// then
		assertThat( getRenderer().isLayerEnabled( layer ) ).isTrue();
		assertThat( getRenderer().getEnabledLayers() ).containsExactly( layer );

		// when
		getRenderer().setLayerEnabled( layer, false );

		// then
		assertThat( getRenderer().isLayerEnabled( layer ) ).isFalse();
		assertThat( getRenderer().getEnabledLayers() ).isEmpty();
	}

	@Test
	void visibleLayers() {
		// given
		DesignLayer first = new DesignLayer();
		DesignLayer second = new DesignLayer();
		List<DesignLayer> layers = List.of( first, second );
		getRenderer().setVisibleLayers( layers );

		// when
		ObservableList<DesignLayer> visibleLayers = renderer.visibleLayers();

		// then
		assertThat( visibleLayers ).isEqualTo( layers );
		assertThat( getRenderer().getVisibleLayers() ).isEqualTo( layers );
		assertThat( getRenderer().isLayerVisible( first ) ).isTrue();
		assertThat( getRenderer().isLayerVisible( second ) ).isTrue();
	}

	@Test
	void setLayerVisible() {
		// given
		DesignLayer layer = new DesignLayer();

		// when
		getRenderer().setLayerVisible( layer, true );

		// then
		assertThat( getRenderer().isLayerVisible( layer ) ).isTrue();
		assertThat( getRenderer().getVisibleLayers() ).containsExactly( layer );

		// when
		getRenderer().setLayerVisible( layer, false );

		// then
		assertThat( getRenderer().isLayerVisible( layer ) ).isFalse();
		assertThat( getRenderer().getVisibleLayers() ).isEmpty();
	}

	@Test
	void selectApertureWithBox() {
		// given
		DesignBox aperture = WINDOW_SELECT_APERTURE;

		// when
		getRenderer().setSelectAperture( aperture );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( aperture );
		assertThat( aperture.getDrawPaint() ).isEqualTo( getRenderer().getApertureDrawPaint() );
		assertThat( aperture.getFillPaint() ).isEqualTo( getRenderer().getApertureFillPaint() );
	}

	@Test
	void selectApertureWithEllipse() {
		// given
		DesignEllipse aperture = RenderConstants.POINT_SELECT_APERTURE;

		// when
		getRenderer().setSelectAperture( aperture );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( aperture );
		assertThat( aperture.getDrawPaint() ).isEqualTo( getRenderer().getApertureDrawPaint() );
		assertThat( aperture.getFillPaint() ).isEqualTo( getRenderer().getApertureFillPaint() );
	}

	@Test
	void defaultSelectAperture() {
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
		assertThat( getRenderer().getSelectAperture().getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( getRenderer().getSelectAperture().getBounds().getWidth() ).isEqualTo( 0 );
		assertThat( getRenderer().getSelectAperture().getBounds().getHeight() ).isEqualTo( 0 );
	}

	@Test
	void selectApertureWithNullUsesEmptyAperture() {
		// given
		getRenderer().setSelectAperture( WINDOW_SELECT_APERTURE );
		assertThat( getRenderer().getSelectAperture() ).isNotEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );

		// when
		getRenderer().setSelectAperture( null );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
	}

	@Test
	void doFindByShapeByIntersect() throws Exception {
		// given
		DesignLayer layer = new DesignLayer();
		getRenderer().setEnabledLayers( List.of( layer ) );
		DesignLine line0 = new DesignLine( -1, -1, 1, 1 );
		DesignLine line1 = new DesignLine( -1, 1, 1, -1 );
		line0.setDrawWidth( "0.5" );
		line1.setDrawWidth( "0.5" );
		layer.addShapes( List.of( line0, line1 ) );
		getRenderer().setLayerVisible( layer, true );

		DesignBox aperture = WINDOW_SELECT_APERTURE;
		aperture.setOrigin( new Point3D( 0, 0, 0 ) );
		aperture.setSize( new Point3D( 4, 4, 0 ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// when
		List<DesignShape> selectedShapes = getRenderer().doFindByShape( aperture, true );

		// then
		assertThat( selectedShapes ).hasSize( 2 );
	}

	@Test
	void doFindByShapeByContains() throws Exception {
		// given
		DesignLayer layer = new DesignLayer();
		getRenderer().setEnabledLayers( List.of( layer ) );
		DesignLine line0 = new DesignLine( -1, -1, 1, 1 );
		DesignLine line1 = new DesignLine( -1, 1, 1, -1 );
		line0.setDrawWidth( "0.5" );
		line1.setDrawWidth( "0.5" );
		layer.addShapes( List.of( line0, line1 ) );
		getRenderer().setLayerVisible( layer, true );

		DesignBox aperture = WINDOW_SELECT_APERTURE;
		aperture.setOrigin( new Point3D( -2, -2, 0 ) );
		aperture.setSize( new Point3D( 4, 4, 0 ) );
		getRenderer().setSelectAperture( aperture );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// when
		List<DesignShape> selectedShapes = getRenderer().doFindByShape( aperture, false );

		// then
		assertThat( selectedShapes ).hasSize( 2 );
	}

	@Test
	void doFindByShapeReducedTestCase() {
		Rectangle box = new Rectangle( -75.59055118110236, -75.59055118110236, 151.1811023622047, 151.1811023622047 );
		Line line = new Line( -37.79527559055118, -37.79527559055118, 37.79527559055118, 37.79527559055118 );

		box.setFill( Paint.valueOf( "0x000000ff" ) );
		line.setStroke( Paint.valueOf( "0x0000ffff" ) );
		line.setStrokeWidth( 18.89763779527559 );

		Shape shape = Shape.intersect( box, line );
		assertThat( ((Path)shape).getElements() ).isNotEmpty();
	}

	// --- Moved from BaseDesignRendererCoverageTest ---

	@Test
	void dpiAndOutputScaleProperties() {
		// defaults
		assertThat( getRenderer().getDpiX() ).isEqualTo( RenderConstants.DEFAULT_DPI );
		assertThat( getRenderer().getDpiY() ).isEqualTo( RenderConstants.DEFAULT_DPI );
		assertThat( getRenderer().getOutputScaleX() ).isEqualTo( RenderConstants.DEFAULT_OUTPUT_SCALE );
		assertThat( getRenderer().getOutputScaleY() ).isEqualTo( RenderConstants.DEFAULT_OUTPUT_SCALE );

		// setters
		getRenderer().setDpi( 110, 120 );
		assertThat( getRenderer().getDpiX() ).isEqualTo( 110 );
		assertThat( getRenderer().getDpiY() ).isEqualTo( 120 );

		getRenderer().setOutputScale( 1.25, 1.5 );
		assertThat( getRenderer().getOutputScaleX() ).isEqualTo( 1.25 );
		assertThat( getRenderer().getOutputScaleY() ).isEqualTo( 1.5 );

		// properties
		getRenderer().dpiXProperty().set( 200 );
		getRenderer().dpiYProperty().set( 300 );
		getRenderer().outputScaleXProperty().set( 2.0 );
		getRenderer().outputScaleYProperty().set( 3.0 );
		assertThat( getRenderer().getDpiX() ).isEqualTo( 200 );
		assertThat( getRenderer().getDpiY() ).isEqualTo( 300 );
		assertThat( getRenderer().getOutputScaleX() ).isEqualTo( 2.0 );
		assertThat( getRenderer().getOutputScaleY() ).isEqualTo( 3.0 );
	}

	@Test
	void viewCenterRotateZoomAndAnchorZoom() {
		// defaults
		assertThat( getRenderer().getViewCenter() ).isEqualTo( RenderConstants.DEFAULT_CENTER );
		assertThat( getRenderer().getViewRotate() ).isEqualTo( RenderConstants.DEFAULT_ROTATE );
		assertThat( getRenderer().getViewZoomX() ).isEqualTo( RenderConstants.DEFAULT_ZOOM );
		assertThat( getRenderer().getViewZoomY() ).isEqualTo( RenderConstants.DEFAULT_ZOOM );

		// setters
		getRenderer().setViewCenter( 10, -5, 2 );
		getRenderer().setViewRotate( 15 );
		getRenderer().setViewZoom( new Point2D( 2.0, 3.0 ) );
		assertThat( getRenderer().getViewCenter() ).isEqualTo( new Point3D( 10, -5, 2 ) );
		assertThat( getRenderer().getViewRotate() ).isEqualTo( 15 );
		assertThat( getRenderer().getViewZoomX() ).isEqualTo( 2.0 );
		assertThat( getRenderer().getViewZoomY() ).isEqualTo( 3.0 );

		// zoom(anchor, factor) must set zoom before center and follow math
		getRenderer().setViewCenter( 10, 0, 0 );
		getRenderer().setViewZoom( 1.0, 1.0 );
		Point3D anchor = new Point3D( 0, 0, 0 );
		getRenderer().zoom( anchor, 2.0 );
		// offset = (10,0,0) - (0,0,0) => (10,0,0); new center = anchor + offset/2 => (5,0,0)
		assertThat( getRenderer().getViewZoomX() ).isEqualTo( 2.0 );
		assertThat( getRenderer().getViewZoomY() ).isEqualTo( 2.0 );
		assertThat( getRenderer().getViewCenter() ).isEqualTo( new Point3D( 5, 0, 0 ) );
	}

	@Test
	void hotspotVisibilityProperty() {
		// default
		assertThat( getRenderer().isHotspotVisible() ).isEqualTo( RenderConstants.DEFAULT_HOTSPOT_VISIBLE );

		// toggle
		getRenderer().setHotspotVisible( true );
		assertThat( getRenderer().isHotspotVisible() ).isTrue();
		getRenderer().hotspotVisible().set( false );
		assertThat( getRenderer().isHotspotVisible() ).isFalse();
	}

	@Test
	void aperturePaintPropagationAndDefaultBehavior() {
		// With default aperture, updating paints should NOT propagate to the default holder
		assertThat( getRenderer().getSelectAperture() ).isSameAs( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
		String originalDraw = getRenderer().getApertureDrawPaint();
		String originalFill = getRenderer().getApertureFillPaint();

		getRenderer().setApertureDrawPaint( "0xff00ffff" );
		getRenderer().setApertureFillPaint( "0x00ffffff" );

		// Default aperture object remains with library default paints
		assertThat( BaseDesignRenderer.DEFAULT_SELECT_APERTURE.getDrawPaint() ).isEqualTo( originalDraw );
		assertThat( BaseDesignRenderer.DEFAULT_SELECT_APERTURE.getFillPaint() ).isEqualTo( originalFill );

		// When a non-default allowed aperture is set, paints should propagate
		getRenderer().setSelectAperture( WINDOW_SELECT_APERTURE );
		getRenderer().setApertureDrawPaint( "0x11223344" );
		getRenderer().setApertureFillPaint( "0x55667788" );
		assertThat( WINDOW_SELECT_APERTURE.getDrawPaint() ).isEqualTo( "0x11223344" );
		assertThat( WINDOW_SELECT_APERTURE.getFillPaint() ).isEqualTo( "0x55667788" );
	}

	@Test
	void unitConversions_realToWorld_and_realToScreen() {
		// Design unit default is CM (see DesignModel.DEFAULT_DESIGN_UNIT)
		// realToWorld: value is converted to model unit and divided by view zoom
		getRenderer().setViewZoom( 2.0, 2.0 );
		DesignValue dvInches = new DesignValue( 1.0, DesignUnit.IN ); // 1 inch
		// Convert to CM first, then divide by zoomX
		double cm = DesignUnit.IN.to( 1.0, DesignUnit.CM ); // 2.54
		double expectedWorld = cm / 2.0; // 1.27

		// Directly call package-private helper via same package access
		double actualWorld = getRenderer().realToWorld( dvInches );
		assertThat( actualWorld ).isEqualTo( expectedWorld );

		// realToScreen: convert to inches then multiply by DPI X
		getRenderer().setDpiX( 96 );
		DesignValue dvCm = new DesignValue( 2.54, DesignUnit.CM ); // 1 inch
		double expectedScreen = 1.0 * 96; // 96 px
		double actualScreen = getRenderer().realToScreen( dvCm );
		assertThat( actualScreen ).isEqualTo( expectedScreen );
	}

}
