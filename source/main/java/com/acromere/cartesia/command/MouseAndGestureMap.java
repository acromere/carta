package com.acromere.cartesia.command;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

public enum MouseAndGestureMap {

	CARTESIA {
		void load( CommandMap map ) {
			map.add( "anchor", new CommandTrigger( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, CommandTrigger.Modifier.ANY ) );

			// Selects ---------------------------------------------------------------
			map.add( "select-point", new CommandTrigger( MouseEvent.MOUSE_RELEASED, MouseButton.PRIMARY ) );
			map.add( "select-toggle", new CommandTrigger( MouseEvent.MOUSE_RELEASED, MouseButton.PRIMARY, CommandTrigger.Modifier.CONTROL ) );
			map.add( "select-window-contain", new CommandTrigger( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, CommandTrigger.Modifier.MOVED ) );
			map.add( "select-window-intersect", new CommandTrigger( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, CommandTrigger.Modifier.SHIFT, CommandTrigger.Modifier.MOVED ) );

			// Snaps -----------------------------------------------------------------
			map.add( "snap-auto-nearest", new CommandTrigger( MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY ) );
			map.add( "snap-auto-midpoint", new CommandTrigger( MouseEvent.MOUSE_CLICKED, MouseButton.MIDDLE ) );

			// Camera 2D -------------------------------------------------------------
			map.add( "camera-move", new CommandTrigger( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, CommandTrigger.Modifier.CONTROL, CommandTrigger.Modifier.MOVED ) );
			map.add( "camera-zoom", new CommandTrigger( ScrollEvent.SCROLL, CommandTrigger.Modifier.CONTROL ) );
			map.add( "camera-zoom", new CommandTrigger( ZoomEvent.ZOOM ) );

			// Camera spin
			//map.add( new CommandEventKey( MouseEvent.DRAG_DETECTED, MouseButton.PRIMARY, false, true, false, false ), "camera-spin" );
			// Camera walk (moving the camera forward and back)
			//map.add( new CommandEventKey( ScrollEvent.SCROLL, false, true, false, false ), "camera-walk" );
			//map.add( new CommandEventKey( ZoomEvent.ZOOM, false, true, false, false ), "camera-walk" );
		}
	};

	abstract void load( CommandMap map );

}
