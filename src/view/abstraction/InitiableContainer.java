package view.abstraction;

import java.util.function.UnaryOperator;

public interface InitiableContainer  {

	public default <T> void initiate(UnaryOperator<T> initiator, T initial) {
		if (initiator != null)
			initiator.apply(initial);
	}
}
