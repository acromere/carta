package com.acromere.cartesia;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel2D;
import com.acromere.xenon.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Design2dResourceTypeTest extends BaseCartesiaUnitTest {

	private Design2dResourceType type;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		type = new Design2dResourceType( getProgram() );
	}

	@Test
	void resourceNew() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.resourceOpen( getProgram(), resource );

		// then
		Design<DesignModel2D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel2D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

	@Test
	void resourceOpen() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.resourceOpen( getProgram(), resource );

		// then
		Design<DesignModel2D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel2D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

	@Test
	void newResourceCanBeModified() {
		// given
		Resource resource = new Resource( "" );
		type.resourceOpen( getProgram(), resource );
		Design<DesignModel2D> design = resource.getModel();
		DesignModel2D model = design.getDataModel();
		assertThat( resource.isModified() ).isFalse();

		// when
		model.getLayers().addLayer( new DesignLayer() );

		// then
		assertThat( resource.isModified() ).isTrue();
	}

}
