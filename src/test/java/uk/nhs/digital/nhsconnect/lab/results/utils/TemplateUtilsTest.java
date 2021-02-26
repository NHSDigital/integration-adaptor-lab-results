package uk.nhs.digital.nhsconnect.lab.results.utils;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.loadTemplate;
import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.fillTemplate;

class TemplateUtilsTest {

    private static final String TEMPLATE_FILLED_WITH_VALUES_PATH =
            "src/test/resources/templates/filled_template_with_value.txt";

    private static final String TEMPLATE_FILLED_NO_VALUES_PATH =
            "src/test/resources/templates/filled_template_no_value.txt";

    private static final String TEMPLATE_NAME = "template_test.mustache";

    @Test
    void testWhenFilledObjectPassedToTemplateThenFilledTemplateIsReturned() throws IOException {
        TemplateTestClass templateTestClass = new TemplateTestClass("Hello");

        Mustache template = loadTemplate(TEMPLATE_NAME);

        String templateResult = fillTemplate(template, templateTestClass);

        String expectedResult = readFile(TEMPLATE_FILLED_WITH_VALUES_PATH);

        assertEquals(expectedResult, templateResult);
    }

    @Test
    void testWhenEmptyObjectPassedToTemplateThenTemplateParametersNotFilled() throws IOException {
        TemplateTestClass templateTestClass = new TemplateTestClass();

        Mustache template = loadTemplate(TEMPLATE_NAME);

        String templateResult = fillTemplate(template, templateTestClass);

        String expectedResult = readFile(TEMPLATE_FILLED_NO_VALUES_PATH);

        assertEquals(expectedResult, templateResult);
    }

    @Test
    void testWhenTemplateNotFoundThenMustacheNotFoundExceptionThrown() {
        TemplateTestClass templateTestClass = new TemplateTestClass();

        assertThrows(MustacheNotFoundException.class, () -> {
            Mustache template = loadTemplate("noTemplate");

            String templateResult = fillTemplate(template, templateTestClass);
        });
    }

    private String readFile(String path) throws IOException {
        Path filePath = FileSystems.getDefault().getPath(path);
        return Files.lines(filePath).collect(Collectors.joining("\n"));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private class TemplateTestClass {

        private String templateValue;
    }
}