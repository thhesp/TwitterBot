package de.ths.twitterbot.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CSVLoaderTest {

    @Mock
    private CSVConfigProperties configProperties;

    @InjectMocks
    private CSVLoader sut;

    @Test
    public void test_loadCSV_filepathIsNull(){
        when(configProperties.getFilepath()).thenReturn(null);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> sut.loadCSVData());
        assertThat(thrown.getMessage(), is("No configuration for the filepath provided."));
    }

    @Test
    public void test_loadCSV_filepathIsEmptyString(){
        when(configProperties.getFilepath()).thenReturn("");

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> sut.loadCSVData());
        assertThat(thrown.getMessage(), is("No configuration for the filepath provided."));
    }

    @Test
    public void test_loadCSV(){
        when(configProperties.getFilepath()).thenReturn("src/test/resources/test.csv");

        List<CSVEntryDTO> entries = sut.loadCSVData();
        assertThat(entries.size(), is(1));
        assertThat(entries.get(0).getMessage(), is("testmessage"));
        assertThat(entries.get(0).getSource(), is("testsource"));
        assertThat(entries.get(0).getHashtags(), is("#test"));
    }
}