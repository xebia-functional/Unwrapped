package continuations;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/*
 * `AtomicReferenceFieldUpdater` cannot be used in Scala currently,
 * see https://contributors.scala-lang.org/t/pre-sip-support-java-util-concurrent-atomic-atomic-fieldupdaters/5287.
 *
 * For this pattern
 * see https://github.com/scala/scala/blob/2.13.x/src/library/scala/collection/concurrent/TrieMap.scala#L520 (`CNode`)
 * and https://github.com/scala/scala/blob/2.13.x/src/library/scala/collection/concurrent/CNodeBase.java#L17.
 */
abstract class SafeContinuationBase {
    @SuppressWarnings("unchecked")
    static final AtomicReferenceFieldUpdater<SafeContinuationBase, Object>  updater =
        AtomicReferenceFieldUpdater.newUpdater(
                (Class<SafeContinuationBase>) (Class<?>) SafeContinuationBase.class,
                (Class<Object>) (Class<?>) Object.class,
                "result");

    volatile Object result = null;

    boolean CAS_RESULT(Object oldval, Object nval) {
        return updater.compareAndSet(this, oldval, nval);
    }
}
