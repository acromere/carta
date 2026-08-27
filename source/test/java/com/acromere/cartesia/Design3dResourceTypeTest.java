package com.acromere.cartesia;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel3D;
import com.acromere.xenon.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Design3dResourceTypeTest extends BaseCartesiaUnitTest {

	private Design3dResourceType type;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		type = new Design3dResourceType( getProgram() );
	}

	@Test
	void assetNew() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.assetOpen( getProgram(), resource );

		// then
		Design<DesignModel3D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel3D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

	@Test
	void assetOpen() {
		// given
		Resource resource = new Resource( "" );

		// when
		type.assetOpen( getProgram(), resource );

		// then
		Design<DesignModel3D> design = resource.getModel();
		assertThat( design ).isNotNull();
		DesignModel3D model = design.getDataModel();
		assertThat( model ).isNotNull();
	}

	@Test
	void newResourceCanBeModified() {
		// given
		Resource resource = new Resource( "" );
		type.assetOpen( getProgram(), resource );
		Design<DesignModel3D> design = resource.getModel();
		DesignModel3D model = design.getDataModel();
		assertThat( resource.isModified() ).isFalse();

		// when
		model.getLayers().addLayer( new DesignLayer() );

		// then
		assertThat( resource.isModified() ).isTrue();
	}

}
