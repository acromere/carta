package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DesignBooleanBindingTest {

	private static class MockNode extends DataNode {

		static final String KEY = "visible";

		static final String OTHER = "other";

		Boolean isVisible() {
			return getValue( KEY, false );
		}

		void setVisible( Boolean visible ) {
			setValue( KEY, visible );
		}

		void setOther( String other ) {
			setValue( OTHER, other );
		}

	}

	@Test
	void testInitialValue() {
		MockNode node = new MockNode();
		node.setVisible( true );

		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );

		assertThat( binding.get() ).isTrue();
		assertThat( binding.getValue() ).isTrue();
	}

	@Test
	void testInitialFalseValue() {
		MockNode node = new MockNode();
		node.setVisible( false );

		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );

		assertThat( binding.get() ).isFalse();
	}

	@Test
	void testUpdateValueOnPropertyChange() {
		MockNode node = new MockNode();
		node.setVisible( true );

		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );
		assertThat( binding.get() ).isTrue();

		node.setVisible( false );
		assertThat( binding.get() ).isFalse();

		node.setVisible( true );
		assertThat( binding.get() ).isTrue();
	}

	@Test
	void testUnrelatedPropertyChangeDoesNotTriggerUpdate() {
		MockNode node = new MockNode();
		node.setVisible( true );

		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );

		AtomicInteger listenerCallCount = new AtomicInteger( 0 );
		binding.addListener( ( observable, oldValue, newValue ) -> listenerCallCount.incrementAndGet() );

		node.setOther( "unrelated" );

		assertThat( listenerCallCount.get() ).isZero();
		assertThat( binding.get() ).isTrue();
	}

	@Test
	void testChangeListenerNotified() {
		MockNode node = new MockNode();
		node.setVisible( true );

		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );

		AtomicInteger callCount = new AtomicInteger( 0 );
		binding.addListener( ( ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue ) -> {
			assertThat( oldValue ).isTrue();
			assertThat( newValue ).isFalse();
			callCount.incrementAndGet();
		} );

		node.setVisible( false );

		assertThat( callCount.get() ).isEqualTo( 1 );
	}

	@Test
	void testGetBeanAndGetName() {
		MockNode node = new MockNode();
		DesignBooleanBinding binding = new DesignBooleanBinding( node, MockNode.KEY, MockNode::isVisible );

		assertThat( binding.getBean() ).isNull();
		assertThat( binding.getName() ).isNull();
	}

}
