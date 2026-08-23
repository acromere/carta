package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.*;
import com.acromere.cartesia.tool.RenderConstants;
import com.acromere.zerra.javafx.Fx;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acromere.cartesia.tool.RenderConstants.WINDOW_SELECT_APERTURE;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseDesignRendererTest {

	private final BaseDesignRenderer renderer;

	protected BaseDesignRendererTest( BaseDesignRenderer renderer ) {
		this.renderer = renderer;
	}

	protected BaseDesignRenderer getRenderer() {
		return renderer;
	}

	@BeforeAll
	static void init() throws Exception {
		Fx.startup();
		Fx.waitFor( 1000 );
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
	void doFindByShapeByIntersect() {
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

		// when
		List<DesignShape> selectedShapes = getRenderer().doFindByShape( aperture, true );

		// then
		assertThat( selectedShapes ).hasSize( 2 );
	}

	@Test
	void doFindByShapeByContains() throws Exception {
		// NEXT This test is flaky on the build box

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
		Fx.waitForStability( 1000 );

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

}
