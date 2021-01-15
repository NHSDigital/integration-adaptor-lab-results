package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split.byColon;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split.byPlus;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split.bySegmentTerminator;

@ExtendWith(SoftAssertionsExtension.class)
class SplitTest {
    @Test
    void When_SplittingBySegmentTerminator_Expect_CorrectResult(SoftAssertions softly) {
        softly.assertThat(bySegmentTerminator("a'q")).containsExactly("a", "q");
        softly.assertThat(bySegmentTerminator("a?'q")).containsExactly("a?'q");
        softly.assertThat(bySegmentTerminator("a??'q")).containsExactly("a??", "q");
        softly.assertThat(bySegmentTerminator("a???'q")).containsExactly("a???'q");
        softly.assertThat(bySegmentTerminator("a???''q")).containsExactly("a???'", "q");
        softly.assertThat(bySegmentTerminator("a?b??'?'??''???'q'")).containsExactly("a?b??", "?'??", "", "???'q", "");
        softly.assertThat(bySegmentTerminator("")).containsExactly("");
        softly.assertThat(bySegmentTerminator("'")).containsExactly("", "");
        softly.assertThat(bySegmentTerminator("''")).containsExactly("", "", "");
        softly.assertThat(bySegmentTerminator("?'?'")).containsExactly("?'?'");
        softly.assertThat(bySegmentTerminator("??'??'")).containsExactly("??", "??", "");
        softly.assertThat(bySegmentTerminator("???'???'")).containsExactly("???'???'");
        softly.assertThat(bySegmentTerminator("??")).containsExactly("??");
        softly.assertThat(bySegmentTerminator("'????")).containsExactly("", "????");
        softly.assertThat(bySegmentTerminator("''????")).containsExactly("", "", "????");
        softly.assertThat(bySegmentTerminator("''???'")).containsExactly("", "", "???'");
        softly.assertThat(bySegmentTerminator("''?''")).containsExactly("", "", "?'", "");
    }

    @Test
    void When_SplittingByColon_Expect_CorrectResult(SoftAssertions softly) {
        softly.assertThat(byColon("asdf:test-string")).containsExactly("asdf", "test-string");
        softly.assertThat(byColon("asdf?:test-string")).containsExactly("asdf?:test-string");
        softly.assertThat(byColon("asdf??:test-string")).containsExactly("asdf??", "test-string");
        softly.assertThat(byColon("asdf???:test-string")).containsExactly("asdf???:test-string");
        softly.assertThat(byColon("asdf????:test-string")).containsExactly("asdf????", "test-string");
    }

    @Test
    void When_SplittingByPlus_Expect_CorrectResult(SoftAssertions softly) {
        softly.assertThat(byPlus("asdf+test-string")).containsExactly("asdf", "test-string");
        softly.assertThat(byPlus("asdf?+test-string")).containsExactly("asdf?+test-string");
        softly.assertThat(byPlus("asdf??+test-string")).containsExactly("asdf??", "test-string");
        softly.assertThat(byPlus("asdf???+test-string")).containsExactly("asdf???+test-string");
        softly.assertThat(byPlus("asdf????+test-string")).containsExactly("asdf????", "test-string");
    }
}