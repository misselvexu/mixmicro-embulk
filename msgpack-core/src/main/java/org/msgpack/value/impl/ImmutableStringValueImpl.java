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

import org.msgpack.value.ImmutableStringValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.util.Objects;

/**
 * {@code ImmutableStringValueImpl} Implements {@code ImmutableStringValue} using a {@code String} field.
 *
 * @see org.msgpack.value.StringValue
 */
public class ImmutableStringValueImpl
        extends AbstractImmutableValue
        implements ImmutableStringValue
{
    private final String string;

    public ImmutableStringValueImpl(String string)
    {
        this.string = string;
    }

    @Override
    public ValueType getValueType()
    {
        return ValueType.STRING;
    }

    @Override
    public ImmutableStringValue immutableValue()
    {
        return this;
    }

    @Override
    public ImmutableStringValue asStringValue()
    {
        return this;
    }

    @Override
    public String asString()
    {
        return string;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Value)) {
            return false;
        }
        Value v = (Value) o;
        if (!v.isStringValue()) {
            return false;
        }

        if (v instanceof ImmutableStringValueImpl) {
            ImmutableStringValueImpl bv = (ImmutableStringValueImpl) v;
            return Objects.equals(string, bv.string);
        }
        else {
            return Objects.equals(string, v.asStringValue().asString());
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(string);
    }

    @Override
    public String toJson()
    {
        StringBuilder sb = new StringBuilder();
        appendJsonString(sb, toString());
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return string;
    }

    static void appendJsonString(StringBuilder sb, String string)
    {
        sb.append("\"");
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch < 0x20) {
                switch (ch) {
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    default:
                        // control chars
                        escapeChar(sb, ch);
                        break;
                }
            }
            else if (ch <= 0x7f) {
                switch (ch) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    default:
                        sb.append(ch);
                        break;
                }
            }
            else if (ch >= 0xd800 && ch <= 0xdfff) {
                // surrogates
                escapeChar(sb, ch);
            }
            else {
                sb.append(ch);
            }
        }
        sb.append("\"");
    }

    private static final char[] HEX_TABLE = "0123456789ABCDEF".toCharArray();

    private static void escapeChar(StringBuilder sb, int ch)
    {
        sb.append("\\u");
        sb.append(HEX_TABLE[(ch >> 12) & 0x0f]);
        sb.append(HEX_TABLE[(ch >> 8) & 0x0f]);
        sb.append(HEX_TABLE[(ch >> 4) & 0x0f]);
        sb.append(HEX_TABLE[ch & 0x0f]);
    }
}
