package com.acromere.cartesia.icon;

public class SnapGridDisabledIcon extends SnapGridIcon {

	public static void main( String[] parameters ) {
		proof( new SnapGridDisabledIcon() );
	}

	protected void define() {
		super.define( false );
	}

}
