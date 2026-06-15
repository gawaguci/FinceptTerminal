package kr.co.mango.stock.signal;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 신호 상태 머신(계획서 11.5).
 *
 * <p>허용된 전이만 수행하며, 잘못된 전이는 예외를 던진다.
 * 종목별로 별도 인스턴스를 사용한다(스레드 안전하지 않음).
 */
public final class SignalStateMachine {

    private static final Map<SignalState, Set<SignalState>> TRANSITIONS = buildTransitions();

    private SignalState state;

    public SignalStateMachine() {
        this.state = SignalState.WAIT;
    }

    public SignalState getState() {
        return state;
    }

    /** 대상 상태로 전이가 가능한지 확인한다. */
    public boolean canTransit(SignalState target) {
        return TRANSITIONS.getOrDefault(state, EnumSet.noneOf(SignalState.class)).contains(target);
    }

    /**
     * 상태를 전이한다.
     *
     * @throws IllegalStateException 허용되지 않은 전이인 경우
     */
    public SignalState transit(SignalState target) {
        if (!canTransit(target)) {
            throw new IllegalStateException(
                    "허용되지 않은 상태 전이: " + state + " -> " + target);
        }
        this.state = target;
        return state;
    }

    private static Map<SignalState, Set<SignalState>> buildTransitions() {
        Map<SignalState, Set<SignalState>> m = new EnumMap<>(SignalState.class);
        m.put(SignalState.WAIT, EnumSet.of(SignalState.WATCH));
        m.put(SignalState.WATCH, EnumSet.of(SignalState.READY, SignalState.WAIT));
        m.put(SignalState.READY, EnumSet.of(SignalState.BUY_SIGNAL, SignalState.WAIT));
        m.put(SignalState.BUY_SIGNAL, EnumSet.of(SignalState.ORDER_WAIT, SignalState.WAIT));
        m.put(SignalState.ORDER_WAIT, EnumSet.of(SignalState.ORDER_BLOCKED, SignalState.ORDER_SENT));
        m.put(SignalState.ORDER_BLOCKED, EnumSet.of(SignalState.WAIT));
        m.put(SignalState.ORDER_SENT, EnumSet.of(SignalState.POSITION_OPEN, SignalState.WAIT));
        m.put(SignalState.POSITION_OPEN, EnumSet.of(SignalState.EXIT_READY));
        m.put(SignalState.EXIT_READY, EnumSet.of(SignalState.POSITION_CLOSED));
        m.put(SignalState.POSITION_CLOSED, EnumSet.of(SignalState.WAIT));
        return m;
    }
}
