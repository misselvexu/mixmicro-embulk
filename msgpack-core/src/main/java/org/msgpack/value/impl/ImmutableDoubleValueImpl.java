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
package org.msgpack.value.impl;

import org.msgpack.value.FloatValue;
import org.msgpack.value.NumberValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@code ImmutableDoubleValueImpl} Implements {@code FloatValue} using a {@code double} field.
 *
 * @see org.msgpack.value.FloatValue
 */
public class ImmutableDoubleValueImpl
        implements FloatValue
{
    private final double value;

    public ImmutableDoubleValueImpl(double value)
    {
        this.value = value;
    }

    @Override
    public ValueType getValueType()
    {
        return ValueType.FLOAT;
    }

    @Override
    public NumberValue asNumberValue()
    {
        return this;
    }

    @Override
    public FloatValue asFloatValue()
    {
        return this;
    }

    @Override
    public byte toByte()
    {
        return (byte) value;
    }

    @Override
    public short toShort()
    {
        return (short) value;
    }

    @Override
    public int toInt()
    {
        return (int) value;
    }

    @Override
    public long toLong()
    {
        return (long) value;
    }

    @Override
    public BigInteger toBigInteger()
    {
        return new BigDecimal(value).toBigInteger();
    }

    @Override
    public float toFloat()
    {
        return (float) value;
    }

    @Override
    public double toDouble()
    {
        return value;
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

        if (!v.isFloatValue()) {
            return false;
        }
        return value == v.asFloatValue().toDouble();
    }

    @Override
    public int hashCode()
    {
        long v = Double.doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public String toJson()
    {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "null";
        }
        else {
            return Double.toString(value);
        }
    }

    @Override
    public String toString()
    {
        return Double.toString(value);
    }
}
