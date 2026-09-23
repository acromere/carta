package com.acromere.cartesia.icon;

public class GridHiddenIcon extends GridIcon {

	public static void main( String[] parameters ) {
		proof( new GridHiddenIcon() );
	}

	protected void define() {
		super.define( false );
	}
}
