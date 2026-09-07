package com.acromere.cartesia.data;

import com.acromere.cartesia.ParseUtil;
import com.acromere.cartesia.math.CadGeometry;
import com.acromere.cartesia.math.CadTransform;
import com.acromere.transaction.Txn;
import com.acromere.transaction.TxnException;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import lombok.CustomLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog
public class DesignQuad extends DesignShape {

	public static final String QUAD = "quad";

	public static final String CONTROL = "control";

	public static final String POINT = "point";

	private static final String LENGTH = "length";

	public DesignQuad() {
		this( null, null, null );
	}

	public DesignQuad( Point3D origin, Point3D originControl, Point3D point ) {
		super( origin );
		addModifyingKeys( CONTROL, POINT );
		setControl( originControl );
		setPoint( point );
	}

	@Override
	public Type getType() {
		return Type.QUAD;
	}

	public Point3D getControl() {
		return getValue( CONTROL );
	}

	public DesignShape setControl( Point3D value ) {
		setValue( CONTROL, value );
		return this;
	}

	public Point3D getPoint() {
		return getValue( POINT );
	}

	public DesignShape setPoint( Point3D point ) {
		setValue( POINT, point );
		return this;
	}

	@Override
	protected Bounds computeGeometricBounds() {
		if( getOrigin() == null || getControl() == null || getPoint() == null ) return null;
		return super.computeGeometricBounds();
	}

	@Override
	public List<Point3D> getReferencePoints() {
		if( getOrigin() == null || getControl() == null || getPoint() == null ) return List.of();
		return List.of( getOrigin(), getControl(), getPoint() );
	}

	@Override
	public double distanceTo( Point3D point ) {
		if( getOrigin() == null || getControl() == null || getPoint() == null || point == null ) return Double.NaN;
		return CadGeometry.pointQuadDistance( point, this );
	}

	@Override
	public double pathLength() {
		if( getOrigin() == null || getControl() == null || getPoint() == null ) return Double.NaN;
		return CadGeometry.quadArcLength( this );
	}

	@Override
	public Map<String, Object> getInformation() {
		Map<String, Object> info = new HashMap<>();
		if( getOrigin() != null ) info.put( ORIGIN, getOrigin() );
		if( getControl() != null ) info.put( CONTROL, getControl() );
		if( getPoint() != null ) info.put( POINT, getPoint() );
		info.put( LENGTH, pathLength() );
		return info;
	}

	@Override
	public DesignQuad cloneShape() {
		return new DesignQuad().copyFrom( this, true );
	}

	@Override
	public void apply( CadTransform transform ) {
		if( getOrigin() == null || getControl() == null || getPoint() == null ) return;

		try( Txn ignored = Txn.create() ) {
			setOrigin( transform.apply( getOrigin() ) );
			setControl( transform.apply( getControl() ) );
			setPoint( transform.apply( getPoint() ) );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to apply transform" );
		}
	}

	protected Map<String, Object> asMap() {
		Map<String, Object> map = super.asMap();
		map.put( SHAPE, QUAD );
		map.putAll( asMap( CONTROL, POINT ) );
		return map;
	}

	@Override
	public DesignQuad updateFrom( Map<String, Object> map ) {
		super.updateFrom( map );
		if( map.containsKey( CONTROL ) ) {
			Object control = map.get( CONTROL );
			if( control instanceof Point3D ) setControl( (Point3D)control );
			else if( control instanceof String ) setControl( ParseUtil.parsePoint3D( (String)control ) );
		}
		if( map.containsKey( POINT ) ) {
			Object point = map.get( POINT );
			if( point instanceof Point3D ) setPoint( (Point3D)point );
			else if( point instanceof String ) setPoint( ParseUtil.parsePoint3D( (String)point ) );
		}
		return this;
	}

	@Override
	public DesignShape updateFrom( DesignShape shape ) {
		super.updateFrom( shape );
		if( !(shape instanceof DesignQuad quad) ) return this;

		try( Txn ignore = Txn.create() ) {
			this.setControl( quad.getControl() );
			this.setPoint( quad.getPoint() );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to update quad" );
		}

		return this;
	}

	@Override
	public String toString() {
		return super.toString( ORIGIN, CONTROL, POINT );
	}

}
