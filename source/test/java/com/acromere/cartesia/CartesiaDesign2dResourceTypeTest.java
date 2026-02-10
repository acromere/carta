package com.acromere.cartesia;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignModel2D;
import com.acromere.xenon.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartesiaDesign2dResourceTypeTest extends BaseCartesiaUnitTest {

	private Design2dResourceType type;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		type = new Design2dResourceType( getProgram() );
	}

	@Test
	void assetNew() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.assetOpen( getProgram(), resource );

		// then
		Design<DesignModel2D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel2D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

	@Test
	void assetOpen() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.assetOpen( getProgram(), resource );

		// then
		Design<DesignModel2D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel2D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

}
