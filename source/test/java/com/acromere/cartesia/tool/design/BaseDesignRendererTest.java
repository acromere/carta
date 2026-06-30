package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLayer;
import com.acromere.zerra.javafx.Fx;
import javafx.collections.ObservableList;
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
}
