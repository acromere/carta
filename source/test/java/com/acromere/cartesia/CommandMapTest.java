package com.acromere.cartesia;

import com.acromere.cartesia.command.CommandMetadata;
import com.acromere.cartesia.command.select.SelectByPoint;
import com.acromere.cartesia.command.select.SelectByWindowContain;
import com.acromere.cartesia.command.select.SelectByWindowIntersect;
import com.acromere.cartesia.command.select.SelectToggle;
import com.acromere.cartesia.command.select.Anchor;
import com.acromere.cartesia.command.camera.CameraMove;
import com.acromere.cartesia.command.camera.CameraZoom;
import com.acromere.cartesia.command.snap.AutoSnap;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandMapTest extends BaseCommandMapTest {

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
	}

	@Test
	void getCommandByShortcut() {
		assertThat( getMod().getCommandMap().getCommandByShortcut( "ws" ).getType() ).isEqualTo( SelectByWindowContain.class );
	}

	@Test
	void getCommandByAction() {
		assertThat( getMod().getCommandMap().getCommandByAction( "anchor" ).getType() ).isEqualTo( Anchor.class );
	}

	@ParameterizedTest
	@MethodSource( "cartesiaMetadataMap" )
	void getCommandByEvent( CommandMetadata expected, InputEvent event ) {
		CommandMetadata actual = getMod().getCommandMap().getCommandByEvent( event );
		assertThat( actual.getAction() ).isEqualTo( expected.getAction() );
		assertThat( actual.getName() ).isEqualTo( expected.getName() );
		assertThat( actual.getCommand() ).isEqualTo( expected.getCommand() );
		assertThat( actual.getShortcut() ).isEqualTo( expected.getShortcut() );
		assertThat( actual.getType() ).isEqualTo( expected.getType() );
	}

	private static Stream<Arguments> cartesiaMetadataMap() {
		return Stream.of(
			// Camera Move
			Arguments.of( createMetadata( "camera-move", "Camera Pan", "pa", CameraMove.class ), createMouseEvent( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, false, true, false, false ) ),

			// Camera Zoom
			Arguments.of( createMetadata( "camera-zoom", "Zoom", "zm", CameraZoom.class ), createScrollEvent( ScrollEvent.SCROLL, false, false, false, false, false, false ) ),

			// Anchor
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, false, false ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, true, false, false, false, false ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, true, false, false, false ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, true, false, false ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, true, false ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, false, true ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, true, false, false, false, true ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, true, false, false, true ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, true, false, true ) ),
//			Arguments.of( createMetadata( "anchor", "Anchor", null, SelectTouch.class ), createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, true, true ) ),

			// Select
			Arguments.of(
				createMetadata( "select-touch", "Select By Point", null, SelectByPoint.class ),
				createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, false )
			),
			Arguments.of(
				createMetadata( "select-toggle", "Select Toggle", null, SelectToggle.class ),
				createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, true, false, false, false )
			),
			Arguments.of(
				createMetadata( "select-window-contain", "Select Window Contain", "ws", SelectByWindowContain.class ),
				createMouseEvent( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, false, false, false, false )
			),
			Arguments.of(
				createMetadata( "select-window-intersect", "Select Window Intersect", "cs", SelectByWindowIntersect.class ),
				createMouseEvent( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, true, false, false, false )
			),

			// Snap Auto Nearest
			Arguments.of( createMetadata( "snap-auto-nearest", "Snap Nearest", null, AutoSnap.class ),
				createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.SECONDARY, false, false, false, false )
			),

			// Snap Auto Midpoint
			Arguments.of( createMetadata( "snap-auto-midpoint", "Snap Midpoint", null, AutoSnap.class ),
				createMouseEvent( MouseEvent.MOUSE_PRESSED, MouseButton.MIDDLE, false, false, false, false )
			)
		);
	}

}
