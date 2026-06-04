package com.acromere.cartesia.command;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public enum MouseAndGestureMap {

	CARTESIA {
		void load( CommandMap map ) {
			map.add( "anchor", new CommandTrigger( MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, CommandTrigger.Modifier.ANY ) );

		}
	};

	abstract void load( CommandMap map );

}
