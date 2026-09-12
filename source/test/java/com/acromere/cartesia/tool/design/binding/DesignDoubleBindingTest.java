package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DesignDoubleBindingTest {

	private static class MockNode extends DataNode {

		static final String KEY = "width";

		static final String OTHER = "other";

		Double getWidth() {
			return getValue( KEY, 0.0 );
		}

		void setWidth( Double width ) {
			setValue( KEY, width );
		}

		void setOther( String other ) {
			setValue( OTHER, other );
		}

	}

	@Test
	void testInitialValue() {
		MockNode node = new MockNode();
		node.setWidth( 42.5 );

		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );

		assertThat( binding.get() ).isEqualTo( 42.5 );
		assertThat( binding.getValue() ).isEqualTo( 42.5 );
	}

	@Test
	void testInitialDefaultValue() {
		MockNode node = new MockNode();

		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );

		assertThat( binding.get() ).isEqualTo( 0.0 );
	}

	@Test
	void testUpdateValueOnPropertyChange() {
		MockNode node = new MockNode();
		node.setWidth( 10.0 );

		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );
		assertThat( binding.get() ).isEqualTo( 10.0 );

		node.setWidth( 25.5 );
		assertThat( binding.get() ).isEqualTo( 25.5 );

		node.setWidth( -5.2 );
		assertThat( binding.get() ).isEqualTo( -5.2 );
	}

	@Test
	void testUnrelatedPropertyChangeDoesNotTriggerUpdate() {
		MockNode node = new MockNode();
		node.setWidth( 10.0 );

		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );

		AtomicInteger listenerCallCount = new AtomicInteger( 0 );
		binding.addListener( ( observable, oldValue, newValue ) -> listenerCallCount.incrementAndGet() );

		node.setOther( "unrelated" );

		assertThat( listenerCallCount.get() ).isZero();
		assertThat( binding.get() ).isEqualTo( 10.0 );
	}

	@Test
	void testChangeListenerNotified() {
		MockNode node = new MockNode();
		node.setWidth( 10.0 );

		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );

		AtomicInteger callCount = new AtomicInteger( 0 );
		binding.addListener( ( ObservableValue<? extends Number> obs, Number oldValue, Number newValue ) -> {
			assertThat( oldValue.doubleValue() ).isEqualTo( 10.0 );
			assertThat( newValue.doubleValue() ).isEqualTo( 20.0 );
			callCount.incrementAndGet();
		} );

		node.setWidth( 20.0 );

		assertThat( callCount.get() ).isEqualTo( 1 );
	}

	@Test
	void testGetBeanAndGetName() {
		MockNode node = new MockNode();
		DesignDoubleBinding binding = new DesignDoubleBinding( node, MockNode.KEY, MockNode::getWidth );

		assertThat( binding.getBean() ).isNull();
		assertThat( binding.getName() ).isNull();
	}

}
