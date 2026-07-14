package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignEllipse;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignShape;
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

	// TODO More enabled layer unit tests
	@Test
	void enabledLayers() {
		// given
		List<DesignLayer> layers = List.of( new DesignLayer() );
		getRenderer().setEnabledLayers( layers );

		// when
		ObservableList<DesignLayer> enabledLayers = renderer.enabledLayers();

		// then
		assertThat( enabledLayers ).isEqualTo( layers );
	}

	// TODO More visible layer unit tests
	@Test
	void visibleLayers() {
		// given
		List<DesignLayer> layers = List.of( new DesignLayer() );
		getRenderer().setVisibleLayers( layers );

		// when
		ObservableList<DesignLayer> visibleLayers = renderer.visibleLayers();

		// then
		assertThat( visibleLayers ).isEqualTo( layers );
	}

	@Test
	void selectApertureDefaultsToEmptyAperture() {
		DesignShape aperture = BaseDesignRenderer.DEFAULT_SELECT_APERTURE;
		assertThat( getRenderer().getSelectAperture() ).isInstanceOf( DesignBox.class );
		assertThat( aperture.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( aperture.getBounds().getWidth() ).isEqualTo( 0 );
		assertThat( aperture.getBounds().getHeight() ).isEqualTo( 0 );
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( aperture );
	}

	@Test
	void selectApertureWithBox() {
		// given
		DesignBox aperture = new DesignBox();

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
		DesignEllipse aperture = new DesignEllipse();

		// when
		getRenderer().setSelectAperture( aperture );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( aperture );
		assertThat( aperture.getDrawPaint() ).isEqualTo( "#00000000" );
		assertThat( aperture.getFillPaint() ).isEqualTo( getRenderer().getApertureFillPaint() );
	}

	@Test
	void selectApertureWithNullUsesEmptyAperture() {
		// given
		getRenderer().setSelectAperture( new DesignBox() );

		// when
		getRenderer().setSelectAperture( null );

		// then
		assertThat( getRenderer().getSelectAperture() ).isEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
	}

}
