package com.acromere.cartesia.icon;

import com.acromere.zerra.color.Colors;
import com.acromere.zerra.image.Proof;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class LayerVisibleIcon extends LayerIcon {

	@Override
	protected void define() {
		super.define();
		fill( getPath(), Colors.mix( (Color)getStrokePaint(), Color.TRANSPARENT, 0.2 ) );
		draw( getPath(), null, 2, StrokeLineCap.BUTT, StrokeLineJoin.MITER );
	}

	public static void main( String[] commands ) {
		Proof.proof( new LayerVisibleIcon() );
	}

}
