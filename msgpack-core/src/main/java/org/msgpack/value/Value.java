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

import org.msgpack.core.MessageTypeCastException;

/**
 * Value stores a value and its type in MessagePack type system.
 *
 * <h2>Type conversion</h2>
 * <p>
 * You can check type first using <b>isXxx()</b> methods or {@link #getValueType()} method, then convert the value to a
 * subtype using <b>asXxx()</b> methods. You can also call asXxx() methods directly and catch
 * {@link org.msgpack.core.MessageTypeCastException}.
 *
 * <table>
 *   <tr><th>MessagePack type</th><th>Check method</th><th>Convert method</th><th>Value type</th></tr>
 *   <tr><td>Nil</td><td>{@link #isNilValue()}</td><td>{@link #asNumberValue()}</td><td>{@link NilValue}</td></tr>
 *   <tr><td>Boolean</td><td>{@link #isBooleanValue()}</td><td>{@link #asBooleanValue()}</td><td>{@link BooleanValue}</td></tr>
 *   <tr><td>Integer or Float</td><td>{@link #isNumberValue()}</td><td>{@link #asNumberValue()}</td><td>{@link NumberValue}</td></tr>
 *   <tr><td>Integer</td><td>{@link #isIntegerValue()}</td><td>{@link #asIntegerValue()}</td><td>{@link IntegerValue}</td></tr>
 *   <tr><td>Float</td><td>{@link #isFloatValue()}</td><td>{@link #asFloatValue()}</td><td>{@link FloatValue}</td></tr>
 *   <tr><td>String</td><td>{@link #isStringValue()}</td><td>{@link #asStringValue()}</td><td>{@link StringValue}</td></tr>
 *   <tr><td>Array</td><td>{@link #isArrayValue()}</td><td>{@link #asArrayValue()}</td><td>{@link ArrayValue}</td></tr>
 *   <tr><td>Map</td><td>{@link #isMapValue()}</td><td>{@link #asMapValue()}</td><td>{@link MapValue}</td></tr>
tr>
 * </table>
 *
 * <h2>Converting to JSON</h2>
 * <p>
 * {@link #toJson()} method returns JSON representation of a Value. See its documents for details.
 * <p>
 * toString() also returns a string representation of a Value that is similar to JSON. However, unlike toJson() method,
 * toString() may return a special format that is not be compatible with JSON when JSON doesn't support the type.
 */
public interface Value
{
    /**
     * Returns type of this value.
     *
     * Note that you can't use <code>instanceof</code> to check type of a value because type of a mutable value is variable.
     */
    ValueType getValueType();

    /**
     * Returns true if type of this value is Nil.
     *
     * If this method returns true, {@code asNilValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((NilValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isNilValue()
    {
        return getValueType().isNilType();
    }

    /**
     * Returns true if type of this value is Boolean.
     *
     * If this method returns true, {@code asBooleanValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((BooleanValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isBooleanValue()
    {
        return getValueType().isBooleanType();
    }

    /**
     * Returns true if type of this value is Integer or Float.
     *
     * If this method returns true, {@code asNumberValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((NumberValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isNumberValue()
    {
        return getValueType().isNumberType();
    }

    /**
     * Returns true if type of this value is Integer.
     *
     * If this method returns true, {@code asIntegerValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((IntegerValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isIntegerValue()
    {
        return getValueType().isIntegerType();
    }

    /**
     * Returns true if type of this value is Float.
     *
     * If this method returns true, {@code asFloatValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((FloatValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isFloatValue()
    {
        return getValueType().isFloatType();
    }

    /**
     * Returns true if type of this value is String.
     *
     * If this method returns true, {@code asStringValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((StringValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isStringValue()
    {
        return getValueType().isStringType();
    }

    /**
     * Returns true if type of this value is Array.
     *
     * If this method returns true, {@code asArrayValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((ArrayValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isArrayValue()
    {
        return getValueType().isArrayType();
    }

    /**
     * Returns true if type of this value is Map.
     *
     * If this method returns true, {@code asMapValue} never throws exceptions.
     * Note that you can't use <code>instanceof</code> or cast <code>((MapValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     */
    default boolean isMapValue()
    {
        return getValueType().isMapType();
    }

    /**
     * Returns the value as {@code NilValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((NilValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Nil.
     */
    default NilValue asNilValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code BooleanValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((BooleanValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Boolean.
     */
    default BooleanValue asBooleanValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code NumberValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((NumberValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Integer or Float.
     */
    default NumberValue asNumberValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code IntegerValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((IntegerValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Integer.
     */
    default IntegerValue asIntegerValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code FloatValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((FloatValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Float.
     */
    default FloatValue asFloatValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code StringValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((StringValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not String.
     */
    default StringValue asStringValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code ArrayValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((ArrayValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Array.
     */
    default ArrayValue asArrayValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Returns the value as {@code MapValue}. Otherwise throws {@code MessageTypeCastException}.
     *
     * Note that you can't use <code>instanceof</code> or cast <code>((MapValue) thisValue)</code> to check type of a value because type of a mutable value is variable.
     *
     * @throws MessageTypeCastException If type of this value is not Map.
     */
    default MapValue asMapValue()
    {
        throw new MessageTypeCastException();
    }

    /**
     * Compares this value to the specified object.
     *
     * This method returns {@code true} if type and value are equivalent.
     * If this value is {@code MapValue} or {@code ArrayValue}, this method check equivalence of elements recursively.
     */
    boolean equals(Object obj);

    /**
     * Returns json representation of this Value.
     * <p>
     * Following behavior is not configurable at this release and they might be changed at future releases:
     *
     * <ul>
     * <li>if a key of MapValue is not string, the key is converted to a string using toString method.</li>
     * <li>NaN and Infinity of DoubleValue are converted to null.</li>
     * <ul>
     */
    String toJson();
}
