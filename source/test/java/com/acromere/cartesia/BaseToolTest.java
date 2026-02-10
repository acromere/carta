package com.acromere.cartesia;

import com.acromere.cartesia.tool.DesignTool;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public abstract class BaseToolTest extends BaseCartesiaUnitTest {

	@Mock
	protected DesignTool tool;

	@Mock
	protected Resource resource;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		lenient().when( tool.getResource() ).thenReturn( resource );
		lenient().when( tool.snapToGrid( any() ) ).then( i -> i.getArgument( 0 ) );
		lenient().when( tool.getScreenToWorldTransform() ).thenReturn( Fx.IDENTITY_TRANSFORM );
	}

}
