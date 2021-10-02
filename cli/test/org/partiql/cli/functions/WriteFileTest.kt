/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *      http://aws.amazon.com/apache2.0/
 *
 *  or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 *  language governing permissions and limitations under the License.
 */

package org.partiql.cli.functions

import com.amazon.ion.system.IonSystemBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.partiql.lang.eval.Bindings
import org.partiql.lang.eval.Environment
import org.partiql.lang.eval.EvaluationSession
import org.partiql.lang.eval.ExprValueFactory
import java.io.File

class WriteFileTest {
    private val ion = IonSystemBuilder.standard().build()
    private val valueFactory = ExprValueFactory.standard(ion)
    private val function = WriteFile(valueFactory)
    private val env = Environment(
        locals = Bindings.empty(),
        session = EvaluationSession.standard()
    )

    // Instead of creating temporary files at a fixed location of /tmp,
    // temporaryFiles is used to track temporary files created using
    // the Java File.createTemporaryFile() mechanism allowing these
    // tests to run on both Windows and Linux...
    private val temporaryFiles: MutableMap<String, File> = mutableMapOf()

    private fun String.exprValue() = valueFactory.newFromIonValue(ion.singleValue(this))
    private fun readFile(testName: String) = temporaryFiles[testName]?.readText()
    private fun dirPath(testName: String) = File.createTempFile("partiqltest", ".ion").also {
        temporaryFiles[testName] = it
    }.invariantSeparatorsPath

    @After
    fun tearDown() {
        temporaryFiles.forEach {
            it.value.delete()
        }
    }

    @Test
    fun writeIonAsDefault() {
        val args = listOf(""" "${dirPath("writeIonAsDefault")}" """, "[1, 2]").map { it.exprValue() }
        function.call(env, args).ionValue

        val expected = "1 2"

        assertEquals(ion.loader.load(expected), ion.loader.load(readFile("writeIonAsDefault")))
    }

    @Test
    fun readIon() {
        val args = listOf(""" "${dirPath("readIon")}" """, """{type: "ion"}""", "[1, 2]").map { it.exprValue() }
        function.call(env, args).ionValue

        val expected = "1 2"

        assertEquals(ion.loader.load(expected), ion.loader.load(readFile("readIon")))
    }
}
