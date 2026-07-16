package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignEllipse;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignNil;
import com.acromere.zerra.javafx.Fx;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

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
	static void init() {
		Fx.startup();
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
		DesignBox aperture = new DesignBox( 0, 0, 0, 0 );

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
		DesignEllipse aperture = new DesignEllipse( Point3D.ZERO, 0.0 );

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
		assertThat( getRenderer().getSelectAperture() ).isInstanceOf( DesignNil.class );
		assertThat( getRenderer().getSelectAperture().getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( getRenderer().getSelectAperture().getBounds().getWidth() ).isEqualTo( 0 );
		assertThat( getRenderer().getSelectAperture().getBounds().getHeight() ).isEqualTo( 0 );
	}

	@Test
	void selectApertureWithNullUsesEmptyAperture() {
		// given
		getRenderer().setSelectAperture( new DesignBox( 0, 0, 0, 0 ) );
		assertThat( getRenderer().getSelectAperture() ).isNotEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );

		// when
		getRenderer().setSelectAperture( null );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
	}

}
