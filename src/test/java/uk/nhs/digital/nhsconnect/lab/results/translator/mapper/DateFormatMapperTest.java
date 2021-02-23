package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.BaseDateTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("checkstyle:magicnumber")
class DateFormatMapperTest {
    @Test
    void testCCYY() {
        var result = new DateFormatMapper().mapToDateTimeType(DateFormat.CCYY, "2021");
        assertThat(result)
            .isNotNull()
            .extracting(BaseDateTimeType::getYear)
            .isEqualTo(2021);
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "1", "202101"})
    void testCCYYFailure(String badDate) {
        assertThatThrownBy(() -> new DateFormatMapper().mapToDateTimeType(DateFormat.CCYY, badDate))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot interpret %s as CCYY", badDate);
    }

    @Test
    void testCCYYMM() {
        var result = new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMM, "202012");
        final var assertResult = assertThat(result).isNotNull();
        assertAll(
            () -> assertResult.extracting(BaseDateTimeType::getYear).isEqualTo(2020),
            () -> assertResult.extracting(BaseDateTimeType::getMonth).isEqualTo(11)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "2", "20210101"})
    void testCCYYMMFailure(String badDate) {
        assertThatThrownBy(() -> new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMM, badDate))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot interpret %s as CCYYMM", badDate);
    }

    @Test
    void testCCYYMMDD() {
        var result = new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMMDD, "20191130");
        final var assertResult = assertThat(result).isNotNull();
        assertAll(
            () -> assertResult.extracting(BaseDateTimeType::getYear).isEqualTo(2019),
            () -> assertResult.extracting(BaseDateTimeType::getMonth).isEqualTo(10),
            () -> assertResult.extracting(BaseDateTimeType::getDay).isEqualTo(30)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "1", "2101011230"})
    void testCCYYMMDDFailure(String badDate) {
        assertThatThrownBy(() -> new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMMDD, badDate))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot interpret %s as CCYYMMDD", badDate);
    }

    @Test
    void testCCYYMMDDHHMM() {
        var result = new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMMDDHHMM, "201810312359");
        final var assertResult = assertThat(result).isNotNull();
        assertAll(
            () -> assertResult.extracting(BaseDateTimeType::getYear).isEqualTo(2018),
            () -> assertResult.extracting(BaseDateTimeType::getMonth).isEqualTo(9),
            () -> assertResult.extracting(BaseDateTimeType::getDay).isEqualTo(31),
            () -> assertResult.extracting(BaseDateTimeType::getHour).isEqualTo(23),
            () -> assertResult.extracting(BaseDateTimeType::getMinute).isEqualTo(59)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "1", "12345678901234"})
    void testCCYYMMDDHHMMFailure(String badDate) {
        assertThatThrownBy(() -> new DateFormatMapper().mapToDateTimeType(DateFormat.CCYYMMDDHHMM, badDate))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot interpret %s as CCYYMMDDHHMM", badDate);
    }
}