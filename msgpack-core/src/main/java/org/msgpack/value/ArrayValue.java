//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.value;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Representation of MessagePack's Array type.
 *
 * MessagePack's Array type can represent sequence of values.
 */
public final class ArrayValue
        implements Value, Iterable<Value>
{
    private static final ArrayValue EMPTY = new ArrayValue(new Value[0]);

    public static ArrayValue empty()
    {
        return EMPTY;
    }

    private final Value[] array;

    public ArrayValue(final Value[] array)
    {
        this.array = array;
    }

    @Override
    public ValueType getValueType()
    {
        return ValueType.ARRAY;
    }

    @Override
    public ArrayValue asArrayValue()
    {
        return this;
    }

    /**
     * Returns number of elements in this array.
     */
    public int size()
    {
        return array.length;
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @throws IndexOutOfBoundsException If the index is out of range
     * (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public Value get(final int index)
    {
        return array[index];
    }

    /**
     * Returns the element at the specified position in this array.
     * This method returns an NilValue if the index is out of range.
     */
    public Value getOrNilValue(final int index)
    {
        if (index < array.length && index >= 0) {
            return array[index];
        }
        return NilValue.get();
    }

    /**
     * Returns an iterator over elements.
     * Returned Iterator does not support {@code remove()} method since the value is immutable.
     */
    public Iterator<Value> iterator()
    {
        return new Ite(array);
    }

    /**
     * Returns the value as {@code List}.
     * Returned List is immutable. It does not support {@code put()}, {@code clear()}, or other methods that modify the value.
     */
    public List<Value> list()
    {
        return new ImmutableArrayValueList(array);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Value)) {
            return false;
        }
        Value v = (Value) o;

        if (v instanceof ArrayValue) {
            ArrayValue oa = (ArrayValue) v;
            return Arrays.equals(array, oa.array);
        }
        else {
            if (!v.isArrayValue()) {
                return false;
            }
            ArrayValue av = v.asArrayValue();
            if (size() != av.size()) {
                return false;
            }
            Iterator<Value> oi = av.iterator();
            int i = 0;
            while (i < array.length) {
                if (!oi.hasNext() || !array[i].equals(oi.next())) {
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    @Override
    public int hashCode()
    {
        int h = 1;
        for (int i = 0; i < array.length; i++) {
            Value obj = array[i];
            h = 31 * h + obj.hashCode();
        }
        return h;
    }

    @Override
    public String toJson()
    {
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(array[0].toJson());
        for (int i = 1; i < array.length; i++) {
            sb.append(",");
            sb.append(array[i].toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString()
    {
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        appendString(sb, array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(",");
            appendString(sb, array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static void appendString(StringBuilder sb, Value value)
    {
        if (value.isStringValue()) {
            sb.append(value.toJson());
        }
        else {
            sb.append(value.toString());
        }
    }

    private static class ImmutableArrayValueList
            extends AbstractList<Value>
    {
        private final Value[] array;

        public ImmutableArrayValueList(Value[] array)
        {
            this.array = array;
        }

        @Override
        public Value get(int index)
        {
            return array[index];
        }

        @Override
        public int size()
        {
            return array.length;
        }
    }

    private static class Ite
            implements Iterator<Value>
    {
        private final Value[] array;
        private int index;

        public Ite(Value[] array)
        {
            this.array = array;
            this.index = 0;
        }

        @Override
        public boolean hasNext()
        {
            return index != array.length;
        }

        @Override
        public Value next()
        {
            int i = index;
            if (i >= array.length) {
                throw new NoSuchElementException();
            }
            index = i + 1;
            return array[i];
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
