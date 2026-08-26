package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUnitTest;
import com.acromere.cartesia.tool.BaseDesignTool;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.acromere.cartesia.tool.RenderConstants.DEFAULT_HOTSPOT_VISIBLE;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseDesignToolTest extends BaseCartesiaUnitTest {

	private BaseDesignTool tool;

	protected BaseDesignTool getTool() {
		return tool;
	}

	protected void setTool( BaseDesignTool tool ) {
		this.tool = tool;
	}

	@Test
	void hotspotVisible_defaultIsFalse() {
		// given a concrete tool provided by subclasses
		BaseDesignTool tool = getTool();
		assertThat( tool ).as( "Tool should be initialized in subclass setup()" ).isNotNull();

		// then default matches render constant (currently false)
		assertThat( tool.isHotspotVisible() ).isEqualTo( DEFAULT_HOTSPOT_VISIBLE );
	}

	@Test
	void hotspotVisible_toggleUpdatesState() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		// when toggle to true
		tool.setHotspotVisible( true );
		// then
		assertThat( tool.isHotspotVisible() ).isTrue();

		// when toggle back to false
		tool.setHotspotVisible( false );
		// then
		assertThat( tool.isHotspotVisible() ).isFalse();
	}

	@Test
	void hotspotVisible_propertyFiresChanges() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		AtomicInteger fired = new AtomicInteger( 0 );
		tool.hotspotVisible().addListener( ( _, _, _ ) -> fired.incrementAndGet() );

		// flip to true (from default false)
		tool.setHotspotVisible( true );
		// flip to false
		tool.setHotspotVisible( false );

		// then at least one change should have fired (exact count may vary if platform coalesces)
		assertThat( fired.get() ).isGreaterThanOrEqualTo( 1 );
		// and final state should be false
		assertThat( tool.isHotspotVisible() ).isFalse();
	}

}
