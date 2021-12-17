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

import org.msgpack.value.impl.ImmutableBigIntegerValueImpl;
import org.msgpack.value.impl.ImmutableBooleanValueImpl;
import org.msgpack.value.impl.ImmutableDoubleValueImpl;
import org.msgpack.value.impl.ImmutableLongValueImpl;
import org.msgpack.value.impl.ImmutableMapValueImpl;
import org.msgpack.value.impl.ImmutableStringValueImpl;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ValueFactory
{
    private ValueFactory()
    {
    }

    public static NilValue newNil()
    {
        return NilValue.get();
    }

    public static BooleanValue newBoolean(boolean v)
    {
        return v ? ImmutableBooleanValueImpl.TRUE : ImmutableBooleanValueImpl.FALSE;
    }

    public static IntegerValue newInteger(byte v)
    {
        return new ImmutableLongValueImpl(v);
    }

    public static IntegerValue newInteger(short v)
    {
        return new ImmutableLongValueImpl(v);
    }

    public static IntegerValue newInteger(int v)
    {
        return new ImmutableLongValueImpl(v);
    }

    public static IntegerValue newInteger(long v)
    {
        return new ImmutableLongValueImpl(v);
    }

    public static IntegerValue newInteger(BigInteger v)
    {
        return new ImmutableBigIntegerValueImpl(v);
    }

    public static FloatValue newFloat(float v)
    {
        return new ImmutableDoubleValueImpl(v);
    }

    public static FloatValue newFloat(double v)
    {
        return new ImmutableDoubleValueImpl(v);
    }

    public static StringValue newString(String s)
    {
        return new ImmutableStringValueImpl(s);
    }

    public static ArrayValue newArray(List<? extends Value> list)
    {
        if (list.isEmpty()) {
            return ArrayValue.empty();
        }
        Value[] array = list.toArray(new Value[list.size()]);
        return new ArrayValue(array);
    }

    public static ArrayValue newArray(Value... array)
    {
        if (array.length == 0) {
            return ArrayValue.empty();
        }
        else {
            return new ArrayValue(Arrays.copyOf(array, array.length));
        }
    }

    public static ArrayValue newArray(Value[] array, boolean omitCopy)
    {
        if (array.length == 0) {
            return ArrayValue.empty();
        }
        else if (omitCopy) {
            return new ArrayValue(array);
        }
        else {
            return new ArrayValue(Arrays.copyOf(array, array.length));
        }
    }

    public static ArrayValue emptyArray()
    {
        return ArrayValue.empty();
    }

    public static <K extends Value, V extends Value>
    MapValue newMap(Map<K, V> map)
    {
        Value[] kvs = new Value[map.size() * 2];
        int index = 0;
        for (Map.Entry<K, V> pair : map.entrySet()) {
            kvs[index] = pair.getKey();
            index++;
            kvs[index] = pair.getValue();
            index++;
        }
        return new ImmutableMapValueImpl(kvs);
    }

    public static MapValue newMap(Value... kvs)
    {
        if (kvs.length == 0) {
            return ImmutableMapValueImpl.empty();
        }
        else {
            return new ImmutableMapValueImpl(Arrays.copyOf(kvs, kvs.length));
        }
    }

    public static MapValue newMap(Value[] kvs, boolean omitCopy)
    {
        if (kvs.length == 0) {
            return ImmutableMapValueImpl.empty();
        }
        else if (omitCopy) {
            return new ImmutableMapValueImpl(kvs);
        }
        else {
            return new ImmutableMapValueImpl(Arrays.copyOf(kvs, kvs.length));
        }
    }

    public static MapValue emptyMap()
    {
        return ImmutableMapValueImpl.empty();
    }

    @SafeVarargs
    public static MapValue newMap(Map.Entry<? extends Value, ? extends Value>... pairs)
    {
        Value[] kvs = new Value[pairs.length * 2];
        for (int i = 0; i < pairs.length; ++i) {
            kvs[i * 2] = pairs[i].getKey();
            kvs[i * 2 + 1] = pairs[i].getValue();
        }
        return newMap(kvs, true);
    }

    public static MapBuilder newMapBuilder()
    {
        return new MapBuilder();
    }

    public static Map.Entry<Value, Value> newMapEntry(Value key, Value value)
    {
        return new AbstractMap.SimpleEntry<Value, Value>(key, value);
    }

    public static class MapBuilder
    {
        private final Map<Value, Value> map = new LinkedHashMap<Value, Value>();

        public MapBuilder()
        {
        }

        public MapValue build()
        {
            return newMap(map);
        }

        public MapBuilder put(Map.Entry<? extends Value, ? extends Value> pair)
        {
            put(pair.getKey(), pair.getValue());
            return this;
        }

        public MapBuilder put(Value key, Value value)
        {
            map.put(key, value);
            return this;
        }

        public MapBuilder putAll(Iterable<? extends Map.Entry<? extends Value, ? extends Value>> entries)
        {
            for (Map.Entry<? extends Value, ? extends Value> entry : entries) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public MapBuilder putAll(Map<? extends Value, ? extends Value> map)
        {
            for (Map.Entry<? extends Value, ? extends Value> entry : map.entrySet()) {
                put(entry);
            }
            return this;
        }
    }
}
