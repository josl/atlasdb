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
package com.palantir.common.base;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.palantir.common.collect.IterableUtils;
import com.palantir.common.collect.IteratorUtils;
import com.palantir.common.proxy.SingleCallProxy;

public class BatchingVisitableFromIterable<T> extends AbstractBatchingVisitable<T> {

    private static final int MAX_BATCH_SIZE = 10000;

    private final Iterable<T> iterable;

    public static <T> BatchingVisitable<T> create(Iterable<? extends T> iterable) {
        return new BatchingVisitableFromIterable<T>(iterable);
    }

    public BatchingVisitableFromIterable(Iterable<? extends T> iterable) {
        this.iterable = IterableUtils.wrap(iterable);
    }

    /**
     * This creates a one time use visitable.  The only proper use of the returned class is to call
     * accept on it directly and never reference it again.
     */
    @SuppressWarnings("unchecked")
    public static <T> BatchingVisitable<T> create(final Iterator<? extends T> iterator) {
        return SingleCallProxy.newProxyInstance(BatchingVisitable.class,
            create(new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return IteratorUtils.wrap(iterator);
                }
            })
        );
    }

    @Override
    public <K extends Exception> void batchAcceptSizeHint(int batchSize,
            ConsistentVisitor<T, K> v) throws K {
        /*
         * Iterables.partition allocates an array of size batchSize, so avoid an OOM by making sure
         * it's not too big.
         */
        batchSize = Math.min(batchSize, MAX_BATCH_SIZE);
        for (List<T> list : Iterables.partition(iterable, batchSize)) {
            if (!v.visit(list)) {
                return;
            }
        }
    }
}
