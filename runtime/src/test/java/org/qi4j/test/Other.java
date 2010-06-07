/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.test;

/**
 * JAVADOC
 */
public interface Other
{
    public String other();

    public String foo( String bar, int blah )
            throws IllegalArgumentException;

    public void bar( double doub, boolean bool, float fl, char ch, int integer, long lg, short sh, byte b, Double doubObj, Object[] objArr, int[] intArr );

    public void multiEx( String bar )
            throws Exception1, Exception2;

    public int unwrapResult();
}