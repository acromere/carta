package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DesignBindingTest {

	private static class MockNode extends DataNode {

		static final String KEY = "key";

		static final String OTHER = "other";

		Paint getPaint() {
			return getValue( KEY );
		}

		void setPaint( Paint paint ) {
			setValue( KEY, paint );
		}

		void setOther( String other ) {
			setValue( OTHER, other );
		}

	}

	@Test
	void testInitialValue() {
		MockNode node = new MockNode();
		node.setPaint( Color.RED );

		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );

		assertThat( binding.get() ).isEqualTo( Color.RED );
		assertThat( binding.getValue() ).isEqualTo( Color.RED );
	}

	@Test
	void testInitialNullValue() {
		MockNode node = new MockNode();

		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );

		assertThat( binding.get() ).isNull();
	}

	@Test
	void testUpdateValueOnPropertyChange() {
		MockNode node = new MockNode();
		node.setPaint( Color.RED );

		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );
		assertThat( binding.get() ).isEqualTo( Color.RED );

		node.setPaint( Color.BLUE );
		assertThat( binding.get() ).isEqualTo( Color.BLUE );

		node.setPaint( null );
		assertThat( binding.get() ).isNull();

		node.setPaint( Color.GREEN );
		assertThat( binding.get() ).isEqualTo( Color.GREEN );
	}

	@Test
	void testUnrelatedPropertyChangeDoesNotTriggerUpdate() {
		MockNode node = new MockNode();
		node.setPaint( Color.RED );

		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );

		AtomicInteger listenerCallCount = new AtomicInteger( 0 );
		binding.addListener( ( observable, oldValue, newValue ) -> listenerCallCount.incrementAndGet() );

		node.setOther( "unrelated" );

		assertThat( listenerCallCount.get() ).isZero();
		assertThat( binding.get() ).isEqualTo( Color.RED );
	}

	@Test
	void testChangeListenerNotified() {
		MockNode node = new MockNode();
		node.setPaint( Color.RED );

		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );

		AtomicReference<Paint> observedOldValue = new AtomicReference<>();
		AtomicReference<Paint> observedNewValue = new AtomicReference<>();
		AtomicInteger callCount = new AtomicInteger( 0 );

		binding.addListener( ( ObservableValue<? extends Paint> obs, Paint oldValue, Paint newValue ) -> {
			observedOldValue.set( oldValue );
			observedNewValue.set( newValue );
			callCount.incrementAndGet();
		} );

		node.setPaint( Color.BLUE );

		assertThat( callCount.get() ).isEqualTo( 1 );
		assertThat( observedOldValue.get() ).isEqualTo( Color.RED );
		assertThat( observedNewValue.get() ).isEqualTo( Color.BLUE );
	}

	@Test
	void testGetBeanAndGetName() {
		MockNode node = new MockNode();
		DesignBinding<Paint> binding = new DesignBinding<>( node, MockNode.KEY, MockNode::getPaint );

		assertThat( binding.getBean() ).isNull();
		assertThat( binding.getName() ).isNull();
	}

}
