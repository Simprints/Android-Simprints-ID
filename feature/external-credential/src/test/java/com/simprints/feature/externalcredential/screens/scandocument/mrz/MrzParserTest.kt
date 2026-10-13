package com.simprints.feature.externalcredential.screens.scandocument.mrz

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

internal class MrzParserTest {
    private lateinit var parser: MrzParser

    @Before
    fun setUp() {
        parser = MrzParser()
    }

    @Test
    fun `check digit matches ICAO 9303 specimen values`() {
        assertThat(MrzParser.checkDigit("L898902C3")).isEqualTo('6')
        assertThat(MrzParser.checkDigit("740812")).isEqualTo('2')
        assertThat(MrzParser.checkDigit("120415")).isEqualTo('9')
        assertThat(MrzParser.checkDigit("ZE184226B<<<<<")).isEqualTo('1')
        assertThat(MrzParser.checkDigit("D23145890")).isEqualTo('7')
    }

    @Test
    fun `parses ICAO 9303 TD3 specimen`() {
        val mrz = parser.parse(TD3_SPECIMEN)

        assertThat(mrz).isEqualTo(
            MrzData(
                format = MrzFormat.TD3,
                documentCode = "P<",
                issuingState = "UTO",
                documentNumber = "L898902C3",
                dateOfBirth = "740812",
                sex = "F",
                dateOfExpiry = "120415",
                nationality = "UTO",
                primaryIdentifier = "ERIKSSON",
                secondaryIdentifier = "ANNA MARIA",
                optionalData1 = "ZE184226B",
                optionalData2 = "",
            ),
        )
    }

    @Test
    fun `parses ICAO 9303 TD1 specimen`() {
        val mrz = parser.parse(TD1_SPECIMEN)

        assertThat(mrz).isEqualTo(
            MrzData(
                format = MrzFormat.TD1,
                documentCode = "I<",
                issuingState = "UTO",
                documentNumber = "D23145890",
                dateOfBirth = "740812",
                sex = "F",
                dateOfExpiry = "120415",
                nationality = "UTO",
                primaryIdentifier = "ERIKSSON",
                secondaryIdentifier = "ANNA MARIA",
                optionalData1 = "",
                optionalData2 = "",
            ),
        )
    }

    @Test
    fun `finds MRZ among other text on the document`() {
        val lines = listOf("PASSPORT", "UTOPIA", "Surname / Nom", "ERIKSSON") + TD3_SPECIMEN + listOf("12345")

        assertThat(parser.parse(lines)?.documentNumber).isEqualTo("L898902C3")
    }

    @Test
    fun `normalises case, whitespace and filler look-alikes`() {
        val lines = listOf(
            "p<utoeriksson<«anna<maria<<<<<<<<<<<<<<<<<<<",
            "L898902C36 UTO7408122F1204159ZE184226B‹‹‹‹‹10",
        )

        assertThat(parser.parse(lines)?.documentNumber).isEqualTo("L898902C3")
    }

    @Test
    fun `restores trailing fillers dropped by OCR`() {
        val lines = listOf(
            TD1_SPECIMEN[0].dropLast(2),
            TD1_SPECIMEN[1],
            TD1_SPECIMEN[2].dropLast(3),
        )

        assertThat(parser.parse(lines)?.documentNumber).isEqualTo("D23145890")
    }

    @Test
    fun `fixes letter-digit confusions in numeric and alphabetic fields`() {
        val lines = listOf(
            TD3_SPECIMEN[0],
            // DOB "740812" read as "74O8I2", nationality "UTO" read as "UT0"
            "L898902C36UT074O8I22F1204159ZE184226B<<<<<10",
        )

        val mrz = parser.parse(lines)

        assertThat(mrz?.dateOfBirth).isEqualTo("740812")
        assertThat(mrz?.nationality).isEqualTo("UTO")
    }

    @Test
    fun `fixes single letter-digit confusion in document number using check digits`() {
        val lines = listOf(
            // "D23145890" read as "D2314589O"
            "I<UTOD2314589O7<<<<<<<<<<<<<<<",
            TD1_SPECIMEN[1],
            TD1_SPECIMEN[2],
        )

        assertThat(parser.parse(lines)?.documentNumber).isEqualTo("D23145890")
    }

    @Test
    fun `rejects MRZ with invalid check digit`() {
        val lines = listOf(
            TD3_SPECIMEN[0],
            // date of expiry check digit changed from 9 to 8
            "L898902C36UTO7408122F1204158ZE184226B<<<<<10",
        )

        assertThat(parser.parse(lines)).isNull()
    }

    @Test
    fun `rejects MRZ with invalid composite check digit`() {
        val lines = listOf(
            TD1_SPECIMEN[0],
            "7408122F1204159UTO<<<<<<<<<<<5",
            TD1_SPECIMEN[2],
        )

        assertThat(parser.parse(lines)).isNull()
    }

    @Test
    fun `rejects incomplete MRZ`() {
        assertThat(parser.parse(TD3_SPECIMEN.take(1))).isNull()
        assertThat(parser.parse(TD1_SPECIMEN.take(1))).isNull()
        assertThat(parser.parse(emptyList())).isNull()
    }

    @Test
    fun `parses TD1 without the names line`() {
        val mrz = parser.parse(TD1_SPECIMEN.take(2))

        assertThat(mrz?.documentNumber).isEqualTo("D23145890")
        assertThat(mrz?.primaryIdentifier).isEmpty()
        assertThat(mrz?.secondaryIdentifier).isEmpty()
    }

    @Test
    fun `parses TD1 with extended document number`() {
        val documentNumber = "D23145890AB1122"
        val extension = documentNumber.drop(9) + MrzParser.checkDigit(documentNumber)
        val line1Data = "${documentNumber.take(9)}<$extension".padEnd(25, '<')
        val line2Prefix = "7408122F1204159UTO"
        val optionalData2 = "<".repeat(11)
        val composite = MrzParser.checkDigit(line1Data + line2Prefix.take(7) + line2Prefix.substring(8, 15) + optionalData2)
        val lines = listOf(
            "I<UTO$line1Data",
            "$line2Prefix$optionalData2$composite",
            TD1_SPECIMEN[2],
        )

        val mrz = parser.parse(lines)

        assertThat(mrz?.documentNumber).isEqualTo(documentNumber)
        assertThat(mrz?.optionalData1).isEmpty()
    }

    @Test
    fun `MrzData string representation does not contain personal data`() {
        val mrz = parser.parse(TD3_SPECIMEN)!!

        assertThat(mrz.toString()).doesNotContain("L898902C3")
        assertThat(mrz.toString()).doesNotContain("ERIKSSON")
        assertThat(mrz.toString()).doesNotContain("740812")
    }

    @Test
    fun `document number pattern accepts ICAO document numbers only`() {
        assertThat(MrzParser.DOCUMENT_NUMBER_PATTERN.matches("L898902C3")).isTrue()
        assertThat(MrzParser.DOCUMENT_NUMBER_PATTERN.matches("D23145890AB1122")).isTrue()
        assertThat(MrzParser.DOCUMENT_NUMBER_PATTERN.matches("L898902C3<")).isFalse()
        assertThat(MrzParser.DOCUMENT_NUMBER_PATTERN.matches("")).isFalse()
    }

    companion object {
        // ICAO Doc 9303 Part 4, Appendix A
        private val TD3_SPECIMEN = listOf(
            "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<",
            "L898902C36UTO7408122F1204159ZE184226B<<<<<10",
        )

        // ICAO Doc 9303 Part 5, Appendix A
        private val TD1_SPECIMEN = listOf(
            "I<UTOD231458907<<<<<<<<<<<<<<<",
            "7408122F1204159UTO<<<<<<<<<<<6",
            "ERIKSSON<<ANNA<MARIA<<<<<<<<<<",
        )
    }
}
