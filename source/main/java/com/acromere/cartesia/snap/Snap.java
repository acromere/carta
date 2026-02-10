package com.acromere.cartesia.snap;

import com.acromere.cartesia.tool.DesignTool;
import javafx.geometry.Point3D;

public interface Snap {

	String getPromptKey();

	Point3D snap( DesignTool tool, Point3D point );

}
