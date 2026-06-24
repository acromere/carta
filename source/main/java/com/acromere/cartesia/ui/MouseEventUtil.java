package com.acromere.cartesia.ui;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MouseEventUtil {

	public static MouseEvent of( Object source, EventTarget target, EventType<MouseEvent> eventType, Point2D local, Point2D screen, MouseButton button ) {
		return of( source, target, eventType, local.getX(), local.getY(), screen.getX(), screen.getY(), button );
	}

	public static MouseEvent of( Object source, EventTarget target, EventType<MouseEvent> eventType, Point3D local, Point2D screen, MouseButton button ) {
		return of( source, target, eventType, local.getX(), local.getY(), screen.getX(), screen.getY(), button );
	}

	public static MouseEvent of( Object source, EventTarget target, EventType<MouseEvent> eventType, double localX, double localY, double screenX, double screenY, MouseButton button ) {
		boolean primaryButton = button == MouseButton.PRIMARY;
		boolean middleButton = button == MouseButton.MIDDLE;
		boolean secondaryButton = button == MouseButton.SECONDARY;
		return new MouseEvent( source, target, eventType, localX, localY, screenX, screenY, button, 1, false, false, false, false, primaryButton, middleButton, secondaryButton, false, false, true, null );
	}

}
