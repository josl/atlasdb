/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.transaction.api;

import com.google.common.collect.ImmutableSet;
import com.palantir.lock.LockRefreshToken;

public class LockAwareTransactionTasks {

    private LockAwareTransactionTasks() {
        // cannot instantiate
    }

    public static <T, E extends Exception> LockAwareTransactionTask<T, E> asLockAware(final TransactionTask<T, E> task) {
        return new LockAwareTransactionTask<T, E>() {
            @Override
            public T execute(Transaction t, Iterable<LockRefreshToken> heldLocks) throws E {
                return task.execute(t);
            }

            @Override
            public String toString() {
                return task.toString();
            }
        };
    }

    public static <T, E extends Exception> TransactionTask<T, E> asLockUnaware(final LockAwareTransactionTask<T, E> task) {
        return asLockUnaware(task, ImmutableSet.<LockRefreshToken>of());
    }

    public static <T, E extends Exception> TransactionTask<T, E> asLockUnaware(final LockAwareTransactionTask<T, E> task, final Iterable<LockRefreshToken> locks) {
        return new TransactionTask<T, E>() {
            @Override
            public T execute(Transaction t) throws E {
                return task.execute(t, locks);
            }
        };
    }
}
