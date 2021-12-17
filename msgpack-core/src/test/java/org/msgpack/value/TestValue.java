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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.msgpack.core.MessageIntegerOverflowException;

public class TestValue {
    @Test
    public void testNilString() {
        assertEquals("null", ValueFactory.newNil().toJson());
        assertEquals("null", ValueFactory.newNil().toString());
    }

    @Test
    public void testBooleanString() {
        assertEquals("true", ValueFactory.newBoolean(true).toJson());
        assertEquals("false", ValueFactory.newBoolean(false).toJson());
        assertEquals("true", ValueFactory.newBoolean(true).toString());
        assertEquals("false", ValueFactory.newBoolean(false).toString());
    }

    @Test
    public void testIntegerString() {
        assertEquals("3", ValueFactory.newInteger(3).toJson());
        assertEquals("3", ValueFactory.newInteger(3).toString());
        assertEquals("1324134134134", ValueFactory.newInteger(BigInteger.valueOf(1324134134134L)).toJson());
        assertEquals("1324134134134", ValueFactory.newInteger(BigInteger.valueOf(1324134134134L)).toString());
    }

    @Test
    public void testFloatString() {
        assertEquals("0.1", ValueFactory.newFloat(0.1).toJson());
        assertEquals("0.1", ValueFactory.newFloat(0.1).toString());
    }

    @Test
    public void testArrayString() {
        assertEquals("[0,\"hello\"]", ValueFactory.newArray(ValueFactory.newInteger(0), ValueFactory.newString("hello")).toJson());
        assertEquals("[0,\"hello\"]", ValueFactory.newArray(ValueFactory.newInteger(0), ValueFactory.newString("hello")).toString());
        assertEquals("[[\"Apple\",0.2],null]", ValueFactory.newArray(ValueFactory.newArray(ValueFactory.newString("Apple"), ValueFactory.newFloat(0.2)), ValueFactory.newNil()).toJson());
    }

    @Test
    public void testMapString() throws IOException {
        // Map value
        final MapValue m = ValueFactory.newMapBuilder()
                .put(ValueFactory.newString("id"), ValueFactory.newInteger(1001))
                .put(ValueFactory.newString("name"), ValueFactory.newString("leo"))
                .put(ValueFactory.newString("address"), ValueFactory.newArray(ValueFactory.newString("xxx-xxxx"), ValueFactory.newString("yyy-yyyy")))
                .put(ValueFactory.newString("name"), ValueFactory.newString("mitsu"))
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode i1 = mapper.readTree(m.toJson());
        final JsonNode i2 = mapper.readTree(m.toString());  // expect json value
        final JsonNode a1 = mapper.readTree("{\"id\":1001,\"name\":\"mitsu\",\"address\":[\"xxx-xxxx\",\"yyy-yyyy\"]}");
        // Equals as JSON map
        assertEquals(a1, i1);
        assertEquals(a1, i2);
    }

    @Test
    public void testStringString() {
        // toJson should quote strings
        assertEquals("\"1\"", ValueFactory.newString("1").toJson());
        // toString is for extracting string values
        assertEquals("1", ValueFactory.newString("1").toString());
    }

    @Test
    public void testIntegerRange() {
        assertEquals(Byte.MAX_VALUE, ValueFactory.newInteger(Byte.MAX_VALUE).asByte());
        assertEquals(Byte.MIN_VALUE, ValueFactory.newInteger(Byte.MIN_VALUE).asByte());
        assertEquals(Short.MAX_VALUE, ValueFactory.newInteger(Short.MAX_VALUE).asShort());
        assertEquals(Short.MIN_VALUE, ValueFactory.newInteger(Short.MIN_VALUE).asShort());
        assertEquals(Integer.MAX_VALUE, ValueFactory.newInteger(Integer.MAX_VALUE).asInt());
        assertEquals(Integer.MIN_VALUE, ValueFactory.newInteger(Integer.MIN_VALUE).asInt());
    }

    @Test
    public void testByteRangeOver() {
        try {
            ValueFactory.newInteger(Byte.MAX_VALUE + 1).asByte();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }

    @Test
    public void testByteRangeUnder() {
        try {
            ValueFactory.newInteger(Byte.MIN_VALUE - 1).asByte();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }

    @Test
    public void testShortRangeOver() {
        try {
            ValueFactory.newInteger(Short.MAX_VALUE + 1).asShort();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }

    @Test
    public void testShortRangeUnder() {
        try {
            ValueFactory.newInteger(Short.MIN_VALUE - 1).asShort();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }

    @Test
    public void testIntegerRangeOver() {
        try {
            ValueFactory.newInteger(Integer.MAX_VALUE + 1L).asInt();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }

    @Test
    public void testIntegerRangeUnder() {
        try {
            ValueFactory.newInteger(Integer.MIN_VALUE - 1L).asInt();
        } catch (final MessageIntegerOverflowException ex) {
            return;
        }
        fail();
    }
}
