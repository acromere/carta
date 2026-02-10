package com.acromere.cartesia.icon;

import com.acromere.zerra.image.Proof;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class LayerCurrentHiddenIcon extends LayerHiddenIcon {

	protected void define() {
		super.define();
		draw( getCurrentPath(), null, 2, StrokeLineCap.BUTT, StrokeLineJoin.MITER );
	}

	public static void main( String[] commands ) {
		Proof.proof( new LayerCurrentHiddenIcon() );
	}

}
