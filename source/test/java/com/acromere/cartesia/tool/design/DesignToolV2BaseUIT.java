package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUiTest;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.xenon.ProgramTool;
import com.acromere.xenon.ProgramToolEvent;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.event.FxEventWatcher;
import com.acromere.zerra.javafx.Fx;
import javafx.stage.Screen;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Getter
@CustomLog
public abstract class DesignToolV2BaseUIT extends BaseCartesiaUiTest {

	private DesignToolV2 tool;

	private Resource resource;

	private DesignModel designModel;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// Load the design asset into a tool
		URI uri = Objects.requireNonNull( getClass().getResource( "/design-tool-test.cartesia2d" ) ).toURI();
		Future<ProgramTool> future = getProgram().getResourceManager().openAsset( uri, DesignToolV2.class );
		tool = (DesignToolV2)future.get();

		// Wait for the tool to be ready
		FxEventWatcher<ProgramToolEvent> eventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ProgramToolEvent.READY, eventWatcher );
		eventWatcher.waitForEvent( ProgramToolEvent.READY );

		Fx.run( () -> tool.setViewZoom( 2 ) );
		Fx.waitForWithExceptions( 1000 );

		this.resource = tool.getResource();
		this.designModel = tool.getDesignModel();

		// Ensure the test resources are available
		assertNotNull( getTool() );
		assertNotNull( getResource() );
		assertNotNull( getDesignModel() );

		// Check the design state
		assertThat( getDesignModel().calcDesignUnit() ).isEqualTo( DesignUnit.CM );
		assertThat( getDesignModel().getAllLayers().size() ).isEqualTo( 10 );

		// Check the tool state
		assertThat( getTool().getDpi() ).isEqualTo( Screen.getPrimary().getDpi() );
		assertThat( getTool().getViewZoom() ).isEqualTo( 2 );
		assertThat( getTool().getVisibleLayers().size() ).isEqualTo( 0 );
		assertThat( getTool().getEnabledLayers().size() ).isEqualTo( 0 );
		assertThat( getTool().getVisibleShapes().size() ).isEqualTo( 0 );

		assertThat( getTool().getSelectTolerance().value() ).isEqualTo( 2 );
		assertThat( getTool().getSelectTolerance().unit() ).isEqualTo( DesignUnit.MM );
	}

	protected double getWorldSelectTolerance() {
		return getTool().getSelectTolerance().to( getDesignModel().calcDesignUnit() ).value() / getTool().getViewZoom();
	}

	protected void useBoxLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e6" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useLineLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e7" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useEllipseLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e9" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useArcLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e8" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useQuadLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ea" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useCubicLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0eb" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void usePathLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ec" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useMarkerLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ed" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

	protected void useTextLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ee" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForWithExceptions( 1000 );
	}

}
