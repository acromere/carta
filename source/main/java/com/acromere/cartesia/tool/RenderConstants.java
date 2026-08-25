package com.acromere.cartesia.tool;

import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignEllipse;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.zerra.color.Colors;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Set;

public interface RenderConstants {

	double DEFAULT_DPI = 96;

	double DEFAULT_OUTPUT_SCALE = 1.0;

	double DEFAULT_UNIT_SCALE = DesignUnit.CM.to( 1, DesignUnit.IN );

	Point3D DEFAULT_CENTER = new Point3D( 0, 0, 0 );

	double DEFAULT_ROTATE = 0;

	double DEFAULT_ZOOM = 1;

	Paint DEFAULT_SELECTED_DRAW_PAINT = Colors.translucent( Color.MAGENTA, 0.8 );

	Paint DEFAULT_SELECTED_FILL_PAINT = Colors.translucent( Color.MAGENTA, 0.2 );

	DesignEllipse POINT_SELECT_APERTURE = new DesignEllipse( 0, 0, 0 );

	DesignBox WINDOW_SELECT_APERTURE = new DesignBox( 0, 0, 0, 0 );

	DesignShape DEFAULT_SELECT_APERTURE = POINT_SELECT_APERTURE;

	Paint DEFAULT_APERTURE_DRAW_PAINT = Colors.translucent( Color.YELLOW, 0.8 );

	Paint DEFAULT_APERTURE_FILL_PAINT = Colors.translucent( Color.YELLOW, 0.2 );

	Set<DesignShape> ALLOWED_SELECT_APERTURES = Set.of( POINT_SELECT_APERTURE, WINDOW_SELECT_APERTURE );

	/**
	 * The default JavaFx refresh rate of 60 Hz.
	 */
	long DEFAULT_REFRESH_RATE = 60;

	/**
	 * The default refresh interval time in nanoseconds.
	 */
	long DEFAULT_REFRESH_TIME_NANOS = 1_000_000_000L / DEFAULT_REFRESH_RATE;

	double CM_PER_INCH = 2.54;

	double INCH_PER_CM = 1 / CM_PER_INCH;

}
