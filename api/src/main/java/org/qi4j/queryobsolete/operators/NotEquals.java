/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.queryobsolete.operators;

import java.util.Map;
import org.qi4j.queryobsolete.BinaryOperator;
import org.qi4j.queryobsolete.BooleanExpression;
import org.qi4j.queryobsolete.Expression;
import org.qi4j.queryobsolete.value.ValueExpression;

public final class NotEquals
    implements BinaryOperator, BooleanExpression
{
    private ValueExpression left;
    private ValueExpression right;

    public NotEquals( ValueExpression left, ValueExpression right )
    {
        this.left = left;
        this.right = right;
    }

    public Expression getLeftArgument()
    {
        return left;
    }

    public Expression getRightArgument()
    {
        return right;
    }

    public boolean evaluate( Object candidate, Map<String, Object> variables )
    {
        Object leftValue = left.getValue( candidate, variables );
        Object rightValue = right.getValue( candidate, variables );
        return !leftValue.equals( rightValue );
    }

    public String toString()
    {
        return "(" + left + " != " + right + ")";
    }
}