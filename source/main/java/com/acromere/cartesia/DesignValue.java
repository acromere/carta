package com.acromere.cartesia;

import org.jspecify.annotations.NonNull;

public record DesignValue(double value, DesignUnit unit) {

	@Override
	@NonNull
	public String toString() {
		return "DesignValue{" + "value=" + value + ", unit=" + unit + '}';
	}

	public DesignValue to( DesignUnit unit ) {
		return new DesignValue( this.unit.to( value, unit ), unit );
	}

}
